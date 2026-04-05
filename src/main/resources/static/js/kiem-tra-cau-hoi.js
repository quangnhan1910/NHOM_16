/**
 * Kiểm tra & Xác nhận - Render danh sách câu hỏi thực từ sessionStorage.
 */
document.addEventListener('DOMContentLoaded', function () {
    const tableBody = document.getElementById('tableBody');
    const summaryText = document.getElementById('summaryText');
    const btnConfirmSave = document.getElementById('btnConfirmSave');
    const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F', 'G'];

    // === 1. Load dữ liệu câu hỏi từ sessionStorage ===
    const raw = sessionStorage.getItem('reviewQuestions');
    if (!raw) {
        summaryText.textContent = 'Không tìm thấy dữ liệu. Vui lòng quay lại trang chỉnh sửa.';
        btnConfirmSave.disabled = true;
        return;
    }

    const questions = JSON.parse(raw);

    // === 2. Validate từng câu hỏi ===
    const validatedQuestions = questions.map((q, index) => {
        const errors = [];

        // Kiểm tra nội dung câu hỏi
        if (!q.questionText || q.questionText.trim().length === 0) {
            errors.push('Thiếu nội dung câu hỏi');
        }

        // Kiểm tra số lượng đáp án
        if (!q.options || q.options.length < 2) {
            errors.push('Cần ít nhất 2 đáp án');
        }

        // Kiểm tra đáp án đúng
        const correctOptions = q.options ? q.options.filter(o => o.isCorrect) : [];
        if (correctOptions.length === 0) {
            errors.push('Chưa chọn đáp án đúng');
        }

        // Kiểm tra đáp án trống
        if (q.options) {
            q.options.forEach((opt, i) => {
                if (!opt.text || opt.text.trim().length === 0) {
                    errors.push(`Đáp án ${optionLabels[i]} trống`);
                }
            });
        }

        return {
            ...q,
            index: index + 1,
            errors: errors,
            isValid: errors.length === 0
        };
    });

    // === 3. Thống kê ===
    const totalCount = validatedQuestions.length;
    const errorCount = validatedQuestions.filter(q => !q.isValid).length;
    const validCount = totalCount - errorCount;

    if (errorCount > 0) {
        summaryText.innerHTML = `Tìm thấy <strong>${totalCount}</strong> câu hỏi. Có <strong class="text-danger">${errorCount}</strong> lỗi cần xử lý.`;
    } else {
        summaryText.innerHTML = `Tìm thấy <strong>${totalCount}</strong> câu hỏi. <strong style="color:#16a34a">Tất cả hợp lệ!</strong>`;
    }

    // === 4. Render bảng ===
    validatedQuestions.forEach(q => {
        const rowDiv = document.createElement('div');
        rowDiv.className = `row item-row ${q.isValid ? '' : 'row-error'}`;

        // Tìm đáp án đúng
        const correctLabels = q.options
            .map((opt, i) => opt.isCorrect ? optionLabels[i] : null)
            .filter(Boolean)
            .join(', ');

        // Loại câu hỏi
        const type = q.isMultipleChoice ? 'Nhiều đáp án' : 'Một đáp án';

        // Render options preview
        const optionsHtml = q.options
            .map((opt, i) => `<span class="opt">${optionLabels[i]}: ${escapeHtml(opt.text)}</span>`)
            .join('');

        // Error box
        let errorBoxHtml = '';
        if (!q.isValid) {
            const errorsLi = q.errors.map(e => `<li>${escapeHtml(e)}</li>`).join('');
            errorBoxHtml = `<div class="error-box"><ul>${errorsLi}</ul></div>`;
        }

        rowDiv.innerHTML = `
            <div class="col col-checkbox"><input type="checkbox"></div>
            <div class="col col-id">${q.index}</div>
            <div class="col col-content">
                <div class="q-main-text ${q.isValid ? '' : 'text-danger'}">${escapeHtml(q.questionText)}</div>
                <div class="q-options">${optionsHtml}</div>
                ${errorBoxHtml}
            </div>
            <div class="col col-type"><span class="tag tag-gray">${type}</span></div>
            <div class="col col-answer"><span class="answer-text">${correctLabels || '-'}</span></div>
            <div class="col col-score">
                <input type="number" class="score-input-cell" data-index="${q.index - 1}" min="0" step="0.5" value="${q.diem != null ? q.diem : ''}" placeholder="điểm">
            </div>
            <div class="col col-diff">
                <select class="diff-select" data-index="${q.index - 1}">
                    <option value="DE">Dễ</option>
                    <option value="TRUNG_BINH" selected>Trung bình</option>
                    <option value="KHO">Khó</option>
                    <option value="RAT_KHO">Rất khó</option>
                </select>
            </div>
            <div class="col col-status">
                <div class="status-icon ${q.isValid ? 'icon-success' : 'icon-danger'}">
                    <i class="fa-solid ${q.isValid ? 'fa-check' : 'fa-xmark'}"></i>
                </div>
            </div>
        `;

        tableBody.appendChild(rowDiv);
    });

    // === 5. Nút Xác nhận Lưu → POST dữ liệu lên API ===
    btnConfirmSave.addEventListener('click', function () {
        if (errorCount > 0) {
            const proceed = confirm(`Có ${errorCount} câu hỏi bị lỗi. Bạn có muốn lưu ${validCount} câu hợp lệ và bỏ qua câu lỗi không?`);
            if (!proceed) return;
        }

        // Thu thập mức độ khó và điểm từ bảng
        const diffSelects = tableBody.querySelectorAll('.diff-select');
        const scoreInputs = tableBody.querySelectorAll('.score-input-cell');
        const cauHois = [];

        validatedQuestions.forEach((q, i) => {
            if (!q.isValid) return; // Bỏ qua câu lỗi

            const diffSelect = diffSelects[i];
            const mucDoKho = diffSelect ? diffSelect.value : 'TRUNG_BINH';

            const scoreInput = scoreInputs[i];
            const diemVal = scoreInput ? parseFloat(scoreInput.value) : null;
            const diem = (!isNaN(diemVal) && diemVal > 0) ? diemVal : null;

            cauHois.push({
                questionText: q.questionText,
                multipleChoice: q.isMultipleChoice,
                mucDoKho: mucDoKho,
                diem: diem,
                options: q.options.map(opt => ({
                    text: opt.text,
                    correct: opt.isCorrect
                }))
            });
        });

        // Lấy mã môn học từ sessionStorage (đã lưu từ trang them-cau-hoi)
        const maMonHoc = parseInt(sessionStorage.getItem('importedMonHocMa')) || 1;

        const originalText = this.textContent;
        this.textContent = 'Đang lưu...';
        this.disabled = true;

        fetch('/api/ngan-hang-cau-hoi/import', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                maMonHoc: maMonHoc,
                cauHois: cauHois
            })
        })
        .then(response => {
            if (!response.ok) throw new Error('Lỗi khi lưu câu hỏi');
            return response.json();
        })
        .then(data => {
            // Xóa dữ liệu tạm
            sessionStorage.removeItem('reviewQuestions');
            sessionStorage.removeItem('importedMonHocMa');
            // Lưu số câu hỏi đã import để hiển thị ở trang thành công
            sessionStorage.setItem('importedCount', data.count);
            window.location.href = '/ngan-hang-cau-hoi/thanh-cong';
        })
        .catch(error => {
            console.error('Lỗi lưu:', error);
            alert('Lưu thất bại! Vui lòng thử lại.');
            this.textContent = originalText;
            this.disabled = false;
        });
    });

    // === 6. Check All ===
    const checkAll = document.getElementById('checkAll');
    if (checkAll) {
        checkAll.addEventListener('change', function () {
            const checkboxes = tableBody.querySelectorAll('input[type="checkbox"]');
            checkboxes.forEach(cb => cb.checked = this.checked);
        });
    }

    // Helper
    function escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
});
