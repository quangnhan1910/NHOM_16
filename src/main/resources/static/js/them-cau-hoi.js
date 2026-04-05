/**
 * Ngân hàng Câu hỏi - Frontend JS cho trang Thêm Câu Hỏi
 */
document.addEventListener('DOMContentLoaded', function () {

    // === DOM Elements ===
    const monHocSelect = document.getElementById('monHocSelect');
    const uploadZone = document.getElementById('uploadZone');
    const fileInput = document.getElementById('fileInput');
    const btnSelectFile = document.getElementById('btnSelectFile');
    const fileInfoContainer = document.getElementById('fileInfoContainer');
    const fileNameDisplay = document.getElementById('fileNameDisplay');
    const btnRemoveFile = document.getElementById('btnRemoveFile');
    const btnSubmitUpload = document.getElementById('btnSubmitUpload');
    const btnDownloadTemplate = document.getElementById('btnDownloadTemplate');

    let currentFile = null;

    // === 1. Load danh sách môn học từ API ===
    function loadMonHoc() {
        fetch('/api/ngan-hang-cau-hoi/mon-hoc')
            .then(response => {
                if (!response.ok) throw new Error('Lỗi khi tải danh sách môn học');
                return response.json();
            })
            .then(monHocs => {
                monHocs.forEach(mh => {
                    const option = document.createElement('option');
                    option.value = mh.ma;
                    option.textContent = mh.ten;
                    monHocSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Lỗi tải môn học:', error);
            });
    }

    loadMonHoc();

    // === 2. Xử lý tải File Mẫu ===
    btnDownloadTemplate.addEventListener('click', function() {
        const link = document.createElement('a');
        link.href = '/files/Mau_Import_Cau_Hoi.doc';
        link.download = 'Mau_Import_Cau_Hoi.doc';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    });

    // === 3. Xử lý Drag & Drop File ===

    // Ngăn chặn hành vi mặc định của trình duyệt
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        uploadZone.addEventListener(eventName, preventDefaults, false);
    });

    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    // Thêm style khi kéo file vào vùng
    ['dragenter', 'dragover'].forEach(eventName => {
        uploadZone.addEventListener(eventName, () => {
            uploadZone.classList.add('dragover');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        uploadZone.addEventListener(eventName, () => {
            uploadZone.classList.remove('dragover');
        }, false);
    });

    // Xử lý khi thả file mềm
    uploadZone.addEventListener('drop', handleDrop, false);

    function handleDrop(e) {
        const dt = e.dataTransfer;
        const files = dt.files;
        handleFiles(files);
    }

    // Xử lý khi bấm nút "Chọn File từ máy tính"
    btnSelectFile.addEventListener('click', () => {
        fileInput.click();
    });

    fileInput.addEventListener('change', function() {
        handleFiles(this.files);
    });

    // Hàm chung xử lý file
    function handleFiles(files) {
        if (files.length === 0) return;
        
        const file = files[0];
        
        // Kiểm tra định dạng hỗ trợ (Excel, Word, PDF)
        const validExtensions = ['.xlsx', '.xls', '.doc', '.docx', '.pdf'];
        const fileName = file.name;
        const fileExt = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();
        
        if (!validExtensions.includes(fileExt)) {
            alert('Định dạng file không được hỗ trợ. Vui lòng chọn file Excel, Word hoặc PDF.');
            return;
        }

        // Lưu file vào biến state
        currentFile = file;

        // Cập nhật UI
        fileNameDisplay.textContent = file.name;
        uploadZone.style.display = 'none';
        fileInfoContainer.style.display = 'flex';
        checkEnableSubmit();
    }

    // Bỏ chọn file
    btnRemoveFile.addEventListener('click', () => {
        currentFile = null;
        fileInput.value = ''; // Reset input
        uploadZone.style.display = 'flex';
        fileInfoContainer.style.display = 'none';
        checkEnableSubmit();
    });

    // === 4. Validation Form ===
    monHocSelect.addEventListener('change', checkEnableSubmit);

    function checkEnableSubmit() {
        const hasMonHoc = monHocSelect.value !== '';
        const hasFile = currentFile !== null;

        if (hasMonHoc && hasFile) {
            btnSubmitUpload.removeAttribute('disabled');
        } else {
            btnSubmitUpload.setAttribute('disabled', 'true');
        }
    }

    // === 5. Submit Upload ===
    btnSubmitUpload.addEventListener('click', function() {
        if (!currentFile || !monHocSelect.value) return;

        const formData = new FormData();
        formData.append('file', currentFile);

        // UI loading
        const originalText = this.textContent;
        this.textContent = 'Đang tải lên...';
        this.disabled = true;

        fetch('/api/ngan-hang-cau-hoi/upload-parse', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) throw new Error('Lỗi khi tải lên file');
            return response.json();
        })
        .then(data => {
            // Lưu rawText và thông tin môn học vào sessionStorage
            sessionStorage.setItem('importedRawText', data.rawText);
            sessionStorage.setItem('importedMonHoc', monHocSelect.options[monHocSelect.selectedIndex].text);
            sessionStorage.setItem('importedMonHocMa', monHocSelect.value);

            // Chuyển sang trình chỉnh sửa
            window.location.href = '/ngan-hang-cau-hoi/chinh-sua';
        })
        .catch(error => {
            console.error('Lỗi upload:', error);
            alert('Tải lên thất bại! Vui lòng kiểm tra lại định dạng file.');
            this.textContent = originalText;
            this.disabled = false;
        });
    });

});
