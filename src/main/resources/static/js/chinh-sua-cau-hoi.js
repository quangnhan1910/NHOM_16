/**
 * Chỉnh sửa nội dung - Sync logic for Raw Editor <-> Live Preview
 */
document.addEventListener('DOMContentLoaded', function () {
    const rawEditor = document.getElementById('rawEditor');
    const lineNumbers = document.getElementById('lineNumbers');
    const livePreviewContainer = document.getElementById('livePreviewContainer');
    const btnSync = document.getElementById('btnSync');

    // === 1. Editor Line Numbers Sync ===
    function updateLineNumbers() {
        const linesCount = rawEditor.value.split('\n').length;
        let numbersHtml = '';
        for (let i = 1; i <= linesCount; i++) {
            numbersHtml += `<div>${i}</div>`;
        }
        lineNumbers.innerHTML = numbersHtml;
    }

    rawEditor.addEventListener('input', updateLineNumbers);
    
    // Sync scroll
    rawEditor.addEventListener('scroll', () => {
        lineNumbers.scrollTop = rawEditor.scrollTop;
    });

    // Initialize line numbers
    updateLineNumbers();

    // === Load imported data from sessionStorage (if any) ===
    const importedRawText = sessionStorage.getItem('importedRawText');
    if (importedRawText) {
        rawEditor.value = importedRawText;
        updateLineNumbers();
        sessionStorage.removeItem('importedRawText');
    }

    // Cập nhật tiêu đề trang nếu có thông tin môn học
    const importedMonHoc = sessionStorage.getItem('importedMonHoc');
    if (importedMonHoc) {
        const pageTitle = document.querySelector('.page-title');
        if (pageTitle) {
            pageTitle.textContent = importedMonHoc;
        }
        sessionStorage.removeItem('importedMonHoc');
        // Giữ lại importedMonHocMa cho bước Kiểm tra & Xác nhận
    }


    // === 2. Parser Logic: Raw Text -> JSON Questions (Keyword-based) ===
    // Nhận diện câu hỏi bằng keyword "Câu X." và đáp án bằng "A.", "*B." v.v.
    function parseRawText(text) {
        const lines = text.split('\n');
        const questions = [];
        let currentQuestion = null;

        // Regex: "Câu 1." hoặc "Câu 12." (có thể có khoảng trắng đầu)
        const questionRegex = /^\s*Câu\s+\d+\.\s*/i;
        // Regex: "A." hoặc "*A." (đáp án, có thể có * phía trước)
        const optionRegex = /^\s*(\*?)([A-D])\.\s*/i;

        for (let i = 0; i < lines.length; i++) {
            const line = lines[i].trim();
            if (line.length === 0) continue; // Bỏ qua dòng trống

            if (questionRegex.test(line)) {
                // === Phát hiện câu hỏi mới ===
                // Lưu câu hỏi trước đó (nếu có)
                if (currentQuestion) {
                    finalizeQuestion(currentQuestion);
                    questions.push(currentQuestion);
                }

                // Tạo câu hỏi mới — bóc bỏ prefix "Câu X."
                const qText = line.replace(questionRegex, '').trim();
                currentQuestion = {
                    questionText: qText,
                    isMultipleChoice: false,
                    options: []
                };

            } else if (optionRegex.test(line) && currentQuestion) {
                // === Phát hiện đáp án ===
                const match = line.match(optionRegex);
                const isCorrect = match[1] === '*';
                const optText = line.replace(optionRegex, '').trim();

                currentQuestion.options.push({
                    text: optText,
                    isCorrect: isCorrect
                });

            } else if (currentQuestion) {
                // Dòng không khớp pattern → nối vào nội dung câu hỏi (câu hỏi dài nhiều dòng)
                currentQuestion.questionText += ' ' + line;
            }
        }

        // Đừng quên câu hỏi cuối cùng
        if (currentQuestion) {
            finalizeQuestion(currentQuestion);
            questions.push(currentQuestion);
        }

        return questions;
    }

    // Xác định loại câu hỏi (nhiều đáp án đúng hay 1)
    function finalizeQuestion(q) {
        const correctCount = q.options.filter(o => o.isCorrect).length;
        q.isMultipleChoice = correctCount > 1;
    }

    // === 3. Render JSON Questions -> HTML Preview (Interactive) ===
    // Lưu trữ state câu hỏi hiện tại để đồng bộ ngược lại editor
    let currentQuestions = [];

    function renderPreview(questions) {
        currentQuestions = questions;
        livePreviewContainer.innerHTML = '';

        questions.forEach((q, qIndex) => {
            const card = document.createElement('div');
            card.className = 'question-card';

            // Header
            const header = document.createElement('div');
            header.className = 'q-header';
            header.innerHTML = `
                <div class="q-badge">Q${qIndex + 1}</div>
                <div class="q-text">${escapeHtml(q.questionText)}</div>
            `;
            card.appendChild(header);

            // Options container
            const optionsContainer = document.createElement('div');
            optionsContainer.className = 'q-options';

            const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];

            q.options.forEach((opt, optIndex) => {
                const optDiv = document.createElement('div');
                optDiv.className = `q-option ${opt.isCorrect ? 'correct' : ''}`;
                optDiv.style.cursor = 'pointer';
                optDiv.title = opt.isCorrect ? 'Nhấn để bỏ chọn đáp án đúng' : 'Nhấn để chọn đáp án đúng';

                const iconClass = q.isMultipleChoice ? 'check-square' : 'radio-circle';

                let checkIconHtml = '';
                if (opt.isCorrect) {
                    checkIconHtml = `<i class="fa-solid fa-circle-check check-icon" style="color:var(--primary)"></i>`;
                }

                optDiv.innerHTML = `
                    <div class="q-opt-left">
                        <div class="${iconClass}"></div>
                        <div class="opt-text">${optionLabels[optIndex]}. ${escapeHtml(opt.text)}</div>
                    </div>
                    ${checkIconHtml}
                `;

                // === Click để toggle đáp án đúng/sai ===
                optDiv.addEventListener('click', () => {
                    toggleCorrectAnswer(qIndex, optIndex);
                });

                optionsContainer.appendChild(optDiv);
            });

            card.appendChild(optionsContainer);
            livePreviewContainer.appendChild(card);
        });
    }

    /**
     * Toggle đáp án đúng/sai và đồng bộ ngược lại rawEditor.
     */
    function toggleCorrectAnswer(qIndex, optIndex) {
        const question = currentQuestions[qIndex];
        if (!question) return;

        // Toggle trạng thái
        question.options[optIndex].isCorrect = !question.options[optIndex].isCorrect;

        // Cập nhật lại isMultipleChoice
        finalizeQuestion(question);

        // Rebuild raw text từ state và ghi lại vào editor
        rawEditor.value = rebuildRawText(currentQuestions);
        updateLineNumbers();

        // Re-render preview
        renderPreview(currentQuestions);
    }

    /**
     * Dựng lại nội dung raw text từ mảng questions (ngược lại parseRawText).
     */
    function rebuildRawText(questions) {
        const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];
        const parts = [];

        questions.forEach((q, qIndex) => {
            // Dòng câu hỏi
            parts.push(`Câu ${qIndex + 1}. ${q.questionText}`);

            // Các đáp án
            q.options.forEach((opt, optIdx) => {
                const prefix = opt.isCorrect ? '*' : '';
                parts.push(`${prefix}${optionLabels[optIdx]}. ${opt.text}`);
            });

            // Dòng trống giữa các câu
            parts.push('');
        });

        return parts.join('\n').trim();
    }

    // === 4. Sync Actions ===
    function syncPreview() {
        // Add spin animation
        const icon = btnSync.querySelector('i');
        icon.classList.add('fa-spin');
        
        const text = rawEditor.value;
        const questions = parseRawText(text);
        renderPreview(questions);

        // Remove spin after tiny delay for effect
        setTimeout(() => icon.classList.remove('fa-spin'), 300);
    }

    // Sync button event
    btnSync.addEventListener('click', syncPreview);

    // === Auto-sync: tự đồng bộ khi gõ/xóa trong editor (debounce 300ms) ===
    let syncTimeout;
    rawEditor.addEventListener('input', function () {
        clearTimeout(syncTimeout);
        syncTimeout = setTimeout(() => {
            const text = rawEditor.value;
            const questions = parseRawText(text);
            renderPreview(questions);
        }, 300);
    });

    // Initial render
    syncPreview();

    // Helper
    function escapeHtml(unsafe) {
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // === 5. Score Widget (Chia điểm) ===
    const scoreLabel = document.getElementById('scoreLabel');
    const scoreInputBox = document.getElementById('scoreInputBox');
    const scoreInput = document.getElementById('scoreInput');
    const scoreValue = document.getElementById('scoreValue');

    // Trạng thái 1: Nhấn "Chia điểm" → hiện ô input
    if (scoreLabel) {
        scoreLabel.addEventListener('click', function () {
            this.style.display = 'none';
            scoreInputBox.style.display = 'block';
            scoreInput.focus();
        });
    }

    // Trạng thái 2 → 3: Nhập xong (blur hoặc Enter) → hiện giá trị
    if (scoreInput) {
        function confirmScore() {
            const val = parseFloat(scoreInput.value);
            if (!isNaN(val) && val > 0) {
                scoreInputBox.style.display = 'none';
                scoreValue.textContent = val + ' điểm';
                scoreValue.style.display = 'inline';
            } else {
                scoreInputBox.style.display = 'none';
                scoreInput.value = '';
                scoreLabel.style.display = 'inline';
            }
        }

        scoreInput.addEventListener('blur', confirmScore);
        scoreInput.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { e.preventDefault(); confirmScore(); }
        });
    }

    // Trạng thái 3: Nhấn vào giá trị → quay lại input để sửa
    if (scoreValue) {
        scoreValue.addEventListener('click', function () {
            this.style.display = 'none';
            scoreInputBox.style.display = 'block';
            scoreInput.focus();
        });
    }

    // === 6. Nút Tiếp tục → Lưu dữ liệu vào sessionStorage và chuyển trang ===
    const btnContinue = document.getElementById('btnContinue');
    if (btnContinue) {
        btnContinue.addEventListener('click', function () {
            const text = rawEditor.value;
            const questions = parseRawText(text);

            if (questions.length === 0) {
                alert('Chưa có câu hỏi nào để kiểm tra. Vui lòng nhập nội dung.');
                return;
            }

            // Chia điểm: tổng điểm / số câu hỏi
            const totalScore = parseFloat(scoreInput ? scoreInput.value : '');
            const scorePerQuestion = !isNaN(totalScore) && totalScore > 0
                ? parseFloat((totalScore / questions.length).toFixed(2))
                : null; // null = chưa chia, để user nhập riêng ở trang kiểm tra

            // Gắn điểm vào mỗi câu
            questions.forEach(q => { q.diem = scorePerQuestion; });

            // Lưu danh sách câu hỏi dạng JSON vào sessionStorage
            sessionStorage.setItem('reviewQuestions', JSON.stringify(questions));
            window.location.href = '/ngan-hang-cau-hoi/kiem-tra';
        });
    }
});
