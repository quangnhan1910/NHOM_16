(function() {
    var API_USER = '/api/nguoi-dung/current';
    var API_PASSWORD = '/api/nguoi-dung/doi-mat-khau';

    function api(url, method, body) {
        return fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        }).then(function(r) { return r.json(); });
    }

    function roleLabel(v) {
        if (!v) return '—';
        if (v === 'QUAN_TRI_VIEN') return 'Quản trị viên';
        if (v === 'GIANG_VIEN') return 'Giảng viên';
        if (v === 'SINH_VIEN') return 'Sinh viên';
        return v;
    }

    // Load current user info
    api(API_USER, 'GET').then(function(result) {
        if (result.success && result.data) {
            var d = result.data;
            var emailEl = document.getElementById('hienThiEmail');
            var vaiTroEl = document.getElementById('hienThiVaiTro');
            if (emailEl) emailEl.textContent = d.thuDienTu || '—';
            if (vaiTroEl) vaiTroEl.textContent = roleLabel(d.vaiTro);
        }
    }).catch(function() {});

    // Toggle password visibility
    document.querySelectorAll('.password-toggle').forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            var passwordField = this.closest('.password-field');
            if (!passwordField) return;
            var input = passwordField.querySelector('input.password-input');
            if (!input) return;
            
            var svg = this.querySelector('svg');
            if (input.type === 'password') {
                input.type = 'text';
                // Change to eye-slash icon
                if (svg) {
                    svg.innerHTML = '<path d="M2 2L16 16M9.88 9.88C9.3 10.47 9 11.2 9 12C9 13.66 10.34 15 12 15C12.8 15 13.53 14.7 14.12 14.12M3 9C3 9 5.5 4 9 4C10.2 4 11.27 4.45 12.13 5.13M16 9C16 9 13.5 14 9 14C8.74 14 8.5 13.97 8.26 13.92" stroke="#64748B" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/><path d="M1 9C1 9 4.5 4 9 4C13.5 4 17 9 17 9C17 9 13.5 14 9 14C4.5 14 1 9 1 9Z" stroke="#64748B" stroke-width="1.5"/><circle cx="9" cy="9" r="2.5" stroke="#64748B" stroke-width="1.5"/>';
                }
            } else {
                input.type = 'password';
                // Change back to eye icon
                if (svg) {
                    svg.innerHTML = '<path d="M2 9C2 9 4.5 4 9 4C13.5 4 16 9 16 9C16 9 13.5 14 9 14C4.5 14 2 9 2 9Z" stroke="#64748B" stroke-width="1.5"/><circle cx="9" cy="9" r="2.5" stroke="#64748B" stroke-width="1.5"/>';
                }
            }
        });
    });

    // Form submit
    var form = document.getElementById('changePasswordForm');
    var submitBtn = form ? form.querySelector('.btn-save') : null;
    var cancelBtn = form ? form.querySelector('.btn-cancel') : null;

    if (submitBtn) {
        submitBtn.addEventListener('click', function(e) {
            e.preventDefault();
            var matKhauHienTai = document.getElementById('current-password') ?
                document.getElementById('current-password').value : '';
            var matKhauMoi = document.getElementById('new-password') ?
                document.getElementById('new-password').value : '';
            var xacNhan = document.getElementById('confirm-password') ?
                document.getElementById('confirm-password').value : '';

            if (!matKhauHienTai || !matKhauMoi || !xacNhan) {
                alert('Vui lòng nhập đầy đủ tất cả các trường mật khẩu.');
                return;
            }
            if (matKhauMoi.length < 6) {
                alert('Mật khẩu mới phải có ít nhất 6 ký tự.');
                return;
            }
            if (matKhauMoi !== xacNhan) {
                alert('Mật khẩu mới và xác nhận mật khẩu mới không khớp.');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang xử lý...';

            api(API_PASSWORD, 'PUT', {
                matKhauHienTai: matKhauHienTai,
                matKhauMoi: matKhauMoi,
                xacNhanMatKhauMoi: xacNhan
            }).then(function(result) {
                if (result.success) {
                    alert('Đổi mật khẩu thành công.');
                    if (form) form.reset();
                } else {
                    alert('Lỗi: ' + (result.message || 'Không xác định.'));
                }
            }).catch(function() {
                alert('Đã xảy ra lỗi khi đổi mật khẩu.');
            }).finally(function() {
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<svg viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M15.5 5L7 13.5L3 9.5" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>Lưu thay đổi';
            });
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', function() {
            if (form) form.reset();
        });
    }
})();
