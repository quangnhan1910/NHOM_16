/**
 * Tạo đề thi mới - Fetch API thay vì hardcode
 */
document.addEventListener('DOMContentLoaded', () => {
    
    // Elements
    const uploadZone = document.getElementById('uploadZone');
    const fileInput = document.getElementById('fileInput');
    const examNameInput = document.getElementById('examName');
    const subjectSelect = document.getElementById('subject');
    const durationInput = document.getElementById('examDuration');
    const btnContinue = document.getElementById('btnContinue');
    
    let selectedFile = null;

    // === 1. Load danh sách môn học từ API ===
    function loadMonHoc() {
        fetch('/api/de-thi/mon-hoc')
            .then(res => res.json())
            .then(data => {
                if (data && data.length > 0) {
                    data.forEach(mh => {
                        const option = document.createElement('option');
                        option.value = mh.ma;
                        option.textContent = mh.ten;
                        if (mh.maDinhDanh) {
                            option.textContent = `${mh.maDinhDanh} - ${mh.ten}`;
                        }
                        subjectSelect.appendChild(option);
                    });
                }
            })
            .catch(err => {
                console.error('Lỗi load danh sách môn học:', err);
            });
    }

    // Gọi ngay khi trang load
    loadMonHoc();

    // === 2. Xử lý Drag & Drop Upload File ===
    
    uploadZone.addEventListener('click', (e) => {
        if(e.target !== fileInput) {
            fileInput.click();
        }
    });

    uploadZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadZone.classList.add('dragover');
    });

    uploadZone.addEventListener('dragleave', () => {
        uploadZone.classList.remove('dragover');
    });

    uploadZone.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadZone.classList.remove('dragover');
        if(e.dataTransfer.files.length > 0) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    });

    fileInput.addEventListener('change', (e) => {
        if(e.target.files.length > 0) {
            handleFileSelect(e.target.files[0]);
        }
    });

    function handleFileSelect(file) {
        // Kiểm tra dung lượng (Max 25MB)
        if(file.size > 25 * 1024 * 1024) {
            alert("Vui lòng chọn file dưới 25MB!");
            return;
        }

        selectedFile = file;
        
        // Update giao diện box Upload
        const uploadTitle = uploadZone.querySelector('.upload-title');
        const uploadSubtitle = uploadZone.querySelector('.upload-subtitle');
        const uploadIconWrapper = uploadZone.querySelector('.upload-icon-wrapper i');
        
        uploadTitle.textContent = file.name;
        uploadSubtitle.textContent = `Kích thước: ${(file.size / (1024*1024)).toFixed(2)} MB`;
        
        uploadIconWrapper.className = file.name.endsWith('.pdf') 
            ? 'fa-solid fa-file-pdf cloud-icon' 
            : 'fa-solid fa-file-word cloud-icon';

        uploadZone.style.borderStyle = 'solid';
        uploadZone.style.borderColor = 'var(--primary-color)';
        uploadZone.style.background = '#F0F4FF';
        
        checkFormValidity();
    }

    // === 3. Ràng buộc form & Mở khoá nút Tiếp Tục ===
    
    examNameInput.addEventListener('input', checkFormValidity);
    subjectSelect.addEventListener('change', checkFormValidity);

    function checkFormValidity() {
        const hasFile = selectedFile !== null;
        const hasName = examNameInput.value.trim() !== '';
        const hasSubject = subjectSelect.value !== '';

        if(hasFile && hasName && hasSubject) {
            btnContinue.classList.add('active');
            btnContinue.removeAttribute('disabled');
        } else {
            btnContinue.classList.remove('active');
            btnContinue.setAttribute('disabled', 'true');
        }
    }

    // === 4. Submit form: gọi API tạo đề thi ===
    btnContinue.addEventListener('click', (e) => {
        if(!btnContinue.classList.contains('active')) {
            e.preventDefault();
            return;
        }

        // Disable nút để tránh click spam
        btnContinue.setAttribute('disabled', 'true');
        btnContinue.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';

        const formData = new FormData();
        formData.append('file', selectedFile);
        formData.append('tenDeThi', examNameInput.value.trim());
        formData.append('maMonHoc', subjectSelect.value);
        
        const duration = durationInput ? durationInput.value : '';
        if (duration && duration.trim() !== '') {
            formData.append('thoiLuongPhut', duration.trim());
        }

        fetch('/api/de-thi/tao-moi', {
            method: 'POST',
            body: formData
        })
        .then(res => {
            if (!res.ok) {
                return res.json().then(data => { throw new Error(data.error || 'Lỗi server'); });
            }
            return res.json();
        })
        .then(data => {
            console.log('Tạo đề thi thành công:', data);

            // Lưu rawText và thông tin đề thi vào sessionStorage để trang chỉnh sửa đọc
            if (data.rawText) {
                sessionStorage.setItem('importedRawText', data.rawText);
            }

            // Lưu mã đề thi để trang chỉnh sửa dùng khi gọi API lưu
            if (data.deThi && data.deThi.ma) {
                sessionStorage.setItem('deThiMa', data.deThi.ma);
            }
            
            // Lưu tên môn học để hiển thị tiêu đề trang chỉnh sửa
            const selectedOption = subjectSelect.options[subjectSelect.selectedIndex];
            if (selectedOption) {
                sessionStorage.setItem('importedMonHoc', examNameInput.value.trim());
            }

            // Chuyển sang trang chỉnh sửa nội dung đề thi
            window.location.href = '/de-thi/sua';
        })
        .catch(err => {
            console.error('Lỗi tạo đề thi:', err);
            alert('Có lỗi xảy ra: ' + err.message);
            
            // Restore nút
            btnContinue.removeAttribute('disabled');
            btnContinue.innerHTML = 'Tiếp tục <i class="fa-solid fa-arrow-right"></i>';
            btnContinue.classList.add('active');
        });
    });
});
