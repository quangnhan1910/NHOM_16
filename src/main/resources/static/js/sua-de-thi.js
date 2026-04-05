/**
 * Chỉnh sửa nội dung đề thi - Sync logic for Raw Editor <-> Live Preview
 */
document.addEventListener('DOMContentLoaded', function () {
    const rawEditor = document.getElementById('rawEditor');
    const lineNumbers = document.getElementById('lineNumbers');
    const livePreviewContainer = document.getElementById('livePreviewContainer');
    const btnSync = document.getElementById('btnSync');

    // === 1. Editor Line Numbers Sync ===
    function updateLineNumbers() {
        if(!rawEditor) return;
        const linesCount = rawEditor.value.split('\n').length;
        let numbersHtml = '';
        for (let i = 1; i <= linesCount; i++) {
            numbersHtml += `<div>${i}</div>`;
        }
        lineNumbers.innerHTML = numbersHtml;
    }

    if (rawEditor) {
        rawEditor.addEventListener('input', updateLineNumbers);
        
        // Sync scroll
        rawEditor.addEventListener('scroll', () => {
            lineNumbers.scrollTop = rawEditor.scrollTop;
        });

        // Initialize line numbers
        updateLineNumbers();
    }

    // === Load imported data from sessionStorage (if any) ===
    const importedRawText = sessionStorage.getItem('importedRawText');
    if (importedRawText && rawEditor) {
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
        if (!text) return [];
        const lines = text.split('\n');
        const questions = [];
        let currentQuestion = null;

        // Regex: "Câu 1." hoặc "Câu 1:" hoặc "Câu 12." (có thể có khoảng trắng đầu)
        const questionRegex = /^\s*Câu\s+\d+[.:]+\s*/i;
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
        if (!livePreviewContainer) return;
        livePreviewContainer.innerHTML = '';

        questions.forEach((q, qIndex) => {
            const card = document.createElement('div');
            card.className = 'question-card';

            // Header with score widget
            const header = document.createElement('div');
            header.className = 'q-header';

            // Left side: badge + question text
            const headerLeft = document.createElement('div');
            headerLeft.className = 'q-header-left';
            headerLeft.innerHTML = `
                <div class="q-badge">Q${qIndex + 1}</div>
                <div class="q-text">${escapeHtml(q.questionText)}</div>
            `;

            // Right side: score widget (3 states)
            const scoreWidget = document.createElement('div');
            scoreWidget.className = 'q-score-widget';

            const currentDiem = q.diem;
            const hasDiem = currentDiem != null && !isNaN(currentDiem) && currentDiem > 0;

            // State 1: Label "Nhập điểm"
            const scoreLabel = document.createElement('span');
            scoreLabel.className = 'q-score-label';
            scoreLabel.textContent = 'Nhập điểm';
            scoreLabel.style.display = hasDiem ? 'none' : 'inline-block';

            // State 2: Input box
            const scoreInputBox = document.createElement('div');
            scoreInputBox.className = 'q-score-input-box';

            const scoreInput = document.createElement('input');
            scoreInput.type = 'number';
            scoreInput.min = '0';
            scoreInput.step = '0.25';
            scoreInput.placeholder = '0';
            if (hasDiem) scoreInput.value = currentDiem;

            const scoreSuffix = document.createElement('span');
            scoreSuffix.className = 'q-score-suffix';
            scoreSuffix.textContent = 'điểm';

            scoreInputBox.appendChild(scoreInput);
            scoreInputBox.appendChild(scoreSuffix);

            // State 3: Display value "X điểm"
            const scoreValue = document.createElement('span');
            scoreValue.className = 'q-score-value';
            scoreValue.textContent = hasDiem ? currentDiem + ' điểm' : '';
            scoreValue.style.display = hasDiem ? 'inline-block' : 'none';

            // === Event: State 1 → State 2 ===
            scoreLabel.addEventListener('click', (e) => {
                e.stopPropagation();
                scoreLabel.style.display = 'none';
                scoreInputBox.style.display = 'flex';
                scoreInput.focus();
            });

            // === Event: State 2 → State 3 (or back to 1) ===
            function confirmQuestionScore() {
                const val = parseFloat(scoreInput.value);
                if (!isNaN(val) && val > 0) {
                    currentQuestions[qIndex].diem = val;
                    scoreInputBox.style.display = 'none';
                    scoreValue.textContent = val + ' điểm';
                    scoreValue.style.display = 'inline-block';
                } else {
                    currentQuestions[qIndex].diem = null;
                    scoreInputBox.style.display = 'none';
                    scoreInput.value = '';
                    scoreLabel.style.display = 'inline-block';
                    scoreValue.style.display = 'none';
                }
            }

            scoreInput.addEventListener('blur', confirmQuestionScore);
            scoreInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') { e.preventDefault(); confirmQuestionScore(); }
            });

            // === Event: State 3 → State 2 (click to edit) ===
            scoreValue.addEventListener('click', (e) => {
                e.stopPropagation();
                scoreValue.style.display = 'none';
                scoreInputBox.style.display = 'flex';
                scoreInput.focus();
            });

            scoreWidget.appendChild(scoreLabel);
            scoreWidget.appendChild(scoreInputBox);
            scoreWidget.appendChild(scoreValue);

            header.appendChild(headerLeft);
            header.appendChild(scoreWidget);
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
        if (!btnSync || !rawEditor) return;
        // Add spin animation
        const icon = btnSync.querySelector('i');
        if (icon) icon.classList.add('fa-spin');
        
        const text = rawEditor.value;
        const questions = parseRawText(text);
        renderPreview(questions);

        // Remove spin after tiny delay for effect
        if (icon) {
            setTimeout(() => icon.classList.remove('fa-spin'), 300);
        }
    }

    // Sync button event
    if (btnSync) {
        btnSync.addEventListener('click', syncPreview);
    }

    // === Auto-sync: tự đồng bộ khi gõ/xóa trong editor (debounce 300ms) ===
    let syncTimeout;
    if (rawEditor) {
        rawEditor.addEventListener('input', function () {
            clearTimeout(syncTimeout);
            syncTimeout = setTimeout(() => {
                const text = rawEditor.value;
                const questions = parseRawText(text);
                renderPreview(questions);
            }, 300);
        });
        
        // Cung cấp data mặc định nếu trống để dễ test
        if(!rawEditor.value) {
            rawEditor.value = "Câu 1. Ví dụ nào sau đây là một ngôn ngữ lập trình bậc cao?\nA. Hợp ngữ\n*B. Python\nC. Mã máy\nD. Mã nhị phân\n\nCâu 2. CPU là viết tắt của từ gì?\n*A. Central Processing Unit\nB. Computer Personal Unit\nC. Central Program Utility\nD. Core Processing Unit\n\nCâu 3. Chọn các loại bộ nhớ chính (Chọn tất cả đáp án đúng):\n*A. RAM\nB. Ổ cứng\n*C. Bộ nhớ Cache\nD. USB Flash Drive";
            updateLineNumbers();
        }
    }

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

                // === Chia đều điểm cho mỗi câu hỏi và cập nhật trực quan ===
                if (currentQuestions.length > 0) {
                    const avg = parseFloat((val / currentQuestions.length).toFixed(2));
                    currentQuestions.forEach(q => {
                        q.diem = avg;
                    });
                    // Re-render preview để hiện điểm trung bình trên mỗi câu
                    renderPreview(currentQuestions);
                }
            } else {
                scoreInputBox.style.display = 'none';
                scoreInput.value = '';
                scoreLabel.style.display = 'inline';

                // Xóa điểm ở các câu chưa được nhập riêng
                currentQuestions.forEach(q => {
                    q.diem = null;
                });
                renderPreview(currentQuestions);
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

    // === 6. Nút Lưu đề thi → Gọi API lưu vào database ===
    const btnContinue = document.getElementById('btnContinue');
    if (btnContinue) {
        btnContinue.addEventListener('click', function () {
            if (!rawEditor) return;
            const text = rawEditor.value;
            const questions = parseRawText(text);

            if (questions.length === 0) {
                alert('Chưa có câu hỏi nào để lưu. Vui lòng nhập nội dung.');
                return;
            }

            // Lấy mã đề thi từ sessionStorage
            const deThiMa = sessionStorage.getItem('deThiMa');
            if (!deThiMa) {
                alert('Không tìm thấy thông tin đề thi. Vui lòng quay lại tạo đề thi mới.');
                return;
            }

            // Lấy điểm tổng (nếu có) từ widget tổng ở header
            const totalScore = parseFloat(scoreInput ? scoreInput.value : '');
            const hasTotal = !isNaN(totalScore) && totalScore > 0;
            const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];

            // Build payload
            const payload = {
                maDeThiHienTai: parseInt(deThiMa),
                tongDiem: hasTotal ? totalScore : null,
                cauHois: questions.map((q, idx) => {
                    const perQ = currentQuestions[idx];
                    const diem = (perQ && perQ.diem != null && perQ.diem > 0)
                        ? perQ.diem
                        : (hasTotal ? parseFloat((totalScore / questions.length).toFixed(2)) : null);

                    return {
                        noiDung: q.questionText,
                        isMultipleChoice: q.isMultipleChoice,
                        diem: diem,
                        luaChons: q.options.map((opt, optIdx) => ({
                            nhan: optionLabels[optIdx],
                            noiDung: opt.text,
                            laDapAnDung: opt.isCorrect
                        }))
                    };
                })
            };

            // Disable nút + loading
            btnContinue.disabled = true;
            btnContinue.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';

            fetch('/api/de-thi/luu', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(res => {
                if (!res.ok) {
                    return res.json().then(data => { throw new Error(data.error || 'Lỗi server'); });
                }
                return res.json();
            })
            .then(data => {
                alert('✅ Lưu đề thi thành công!');
                // Xóa dữ liệu tạm
                sessionStorage.removeItem('deThiMa');
                sessionStorage.removeItem('importedRawText');
                sessionStorage.removeItem('importedMonHoc');
                // Quay về trang quản lý đề thi
                window.location.href = '/de-thi/quan-ly';
            })
            .catch(err => {
                console.error('Lỗi lưu đề thi:', err);
                alert('❌ Có lỗi xảy ra: ' + err.message);
                btnContinue.disabled = false;
                btnContinue.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Lưu đề thi';
            });
        });
    }

    // === 7. Nút Lưu bản nháp → Gọi API lưu đề thi vào database ===
    const btnSaveDraft = document.getElementById('btnSaveDraft');
    if (btnSaveDraft) {
        btnSaveDraft.addEventListener('click', function () {
            if (!rawEditor) return;
            const text = rawEditor.value;
            const questions = parseRawText(text);

            if (questions.length === 0) {
                alert('Chưa có câu hỏi nào để lưu. Vui lòng nhập nội dung.');
                return;
            }

            // Lấy mã đề thi từ sessionStorage (được lưu khi tạo đề thi ở bước trước)
            const deThiMa = sessionStorage.getItem('deThiMa');
            if (!deThiMa) {
                alert('Không tìm thấy thông tin đề thi. Vui lòng quay lại tạo đề thi mới.');
                return;
            }

            // Lấy tổng điểm
            const totalScore = parseFloat(scoreInput ? scoreInput.value : '');
            const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];

            // Build payload theo LuuDeThiRequest
            const payload = {
                maDeThiHienTai: parseInt(deThiMa),
                tongDiem: !isNaN(totalScore) && totalScore > 0 ? totalScore : null,
                cauHois: questions.map((q, idx) => {
                    // Lấy điểm riêng từ currentQuestions
                    const perQ = currentQuestions[idx];
                    const diem = (perQ && perQ.diem != null && perQ.diem > 0) 
                        ? perQ.diem 
                        : (!isNaN(totalScore) && totalScore > 0 
                            ? parseFloat((totalScore / questions.length).toFixed(2)) 
                            : null);

                    return {
                        noiDung: q.questionText,
                        isMultipleChoice: q.isMultipleChoice,
                        diem: diem,
                        luaChons: q.options.map((opt, optIdx) => ({
                            nhan: optionLabels[optIdx],
                            noiDung: opt.text,
                            laDapAnDung: opt.isCorrect
                        }))
                    };
                })
            };

            // Disable nút + loading
            btnSaveDraft.disabled = true;
            const originalText = btnSaveDraft.textContent;
            btnSaveDraft.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';

            fetch('/api/de-thi/luu', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            })
            .then(res => {
                if (!res.ok) {
                    return res.json().then(data => { throw new Error(data.error || 'Lỗi server'); });
                }
                return res.json();
            })
            .then(data => {
                alert('Lưu đề thi thành công!');
                console.log('Đề thi đã lưu:', data);

                // Chuyển về trang quản lý
                window.location.href = '/de-thi/quan-ly';
            })
            .catch(err => {
                console.error('Lỗi lưu đề thi:', err);
                alert('Có lỗi xảy ra: ' + err.message);

                // Restore nút
                btnSaveDraft.disabled = false;
                btnSaveDraft.textContent = originalText;
            });
        });
    }
});
