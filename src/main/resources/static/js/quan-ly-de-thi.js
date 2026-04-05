/**
 * Quản lý đề thi - Load dữ liệu thực từ API
 */
document.addEventListener('DOMContentLoaded', () => {

    // === DOM Elements ===
    const examTableBody  = document.getElementById('examTableBody');
    const searchInput    = document.getElementById('searchInput');
    const pageInfo       = document.getElementById('pageInfo');
    const pageControls   = document.getElementById('pageControls');
    const countAll       = document.getElementById('countAll');
    const countPublished = document.getElementById('countPublished');
    const countDraft     = document.getElementById('countDraft');
    const tabs           = document.querySelectorAll('.tab-item');
    const btnFilter      = document.getElementById('btnFilter');

    // === State ===
    let currentTab  = 'all';   // all | published | draft
    let currentPage = 0;
    let pageSize    = 10;
    let currentKeyword = '';

    // === 1. Load thống kê (số lượng trên tabs) ===
    function loadThongKe() {
        fetch('/api/de-thi/thong-ke')
            .then(res => res.json())
            .then(data => {
                if (countAll)       countAll.textContent       = data.total || 0;
                if (countPublished) countPublished.textContent = data.published || 0;
                if (countDraft)     countDraft.textContent     = data.draft || 0;
            })
            .catch(err => console.error('Lỗi load thống kê:', err));
    }

    // === 2. Load danh sách đề thi ===
    function loadDanhSach() {
        // Hiển thị loading
        examTableBody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align:center; padding:40px; color:#94A3B8;">
                    <i class="fa-solid fa-spinner fa-spin"></i> Đang tải dữ liệu...
                </td>
            </tr>`;

        // Build URL params
        const params = new URLSearchParams();
        params.append('page', currentPage);
        params.append('size', pageSize);

        if (currentTab === 'published') {
            params.append('trangThai', 'published');
        } else if (currentTab === 'draft') {
            params.append('trangThai', 'draft');
        }

        if (currentKeyword && currentKeyword.trim() !== '') {
            params.append('tuKhoa', currentKeyword.trim());
        }

        fetch(`/api/de-thi/danh-sach?${params.toString()}`)
            .then(res => res.json())
            .then(data => {
                renderTable(data);
                renderPagination(data);
            })
            .catch(err => {
                console.error('Lỗi load danh sách:', err);
                examTableBody.innerHTML = `
                    <tr>
                        <td colspan="6" style="text-align:center; padding:40px; color:#EF4444;">
                            <i class="fa-solid fa-triangle-exclamation"></i> Không thể tải dữ liệu. Vui lòng thử lại.
                        </td>
                    </tr>`;
            });
    }

    // === 3. Render bảng dữ liệu ===
    function renderTable(pageData) {
        const items = pageData.content;
        
        if (!items || items.length === 0) {
            examTableBody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align:center; padding:40px; color:#94A3B8;">
                        <i class="fa-solid fa-inbox" style="font-size:24px; display:block; margin-bottom:8px;"></i>
                        Chưa có đề thi nào
                    </td>
                </tr>`;
            return;
        }

        let html = '';
        items.forEach(item => {
            const tenDeThi   = escapeHtml(item.tenDeThi || 'Chưa đặt tên');
            const maDeThi    = 'Mã đề: ' + (item.ma || '---');
            const tenMonHoc  = escapeHtml(item.tenMonHoc || 'Chưa gán');
            const thoiLuong  = item.thoiLuongPhut ? (item.thoiLuongPhut + ' phút') : '---';
            const tongDiem   = item.tongDiem != null ? item.tongDiem : '---';
            const daXuatBan  = item.daXuatBan;

            // CSS class cho badge trạng thái
            const statusClass = daXuatBan ? 'status-published' : 'status-draft';
            const statusText  = daXuatBan ? 'Đã xuất bản' : 'Bản nháp';

            html += `
                <tr>
                    <td>
                        <div class="exam-name">${tenDeThi}</div>
                        <div class="exam-code">${maDeThi}</div>
                    </td>
                    <td><span class="subject-badge">${tenMonHoc}</span></td>
                    <td class="exam-duration">${thoiLuong}</td>
                    <td style="text-align:center;" class="exam-score">${tongDiem}</td>
                    <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                    <td style="text-align:center;">
                        <div class="action-wrapper">
                            <button class="action-menu" data-ma="${item.ma}">
                                <i class="fa-solid fa-ellipsis-vertical"></i>
                            </button>
                            <div class="action-dropdown" id="dropdown-${item.ma}">
                                <button class="action-dropdown-item btn-edit" data-ma="${item.ma}">
                                    <i class="fa-solid fa-pen-to-square"></i> Sửa
                                </button>
                                <button class="action-dropdown-item delete btn-delete" data-ma="${item.ma}">
                                    <i class="fa-solid fa-trash"></i> Xóa
                                </button>
                            </div>
                        </div>
                    </td>
                </tr>`;
        });

        examTableBody.innerHTML = html;

        // === Toggle dropdown menu ===
        document.querySelectorAll('.action-menu').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const ma = btn.getAttribute('data-ma');
                // Đóng tất cả dropdown khác
                document.querySelectorAll('.action-dropdown.show').forEach(d => d.classList.remove('show'));
                const dropdown = document.getElementById('dropdown-' + ma);
                if (dropdown) dropdown.classList.toggle('show');
            });
        });

        // === Nút Sửa ===
        document.querySelectorAll('.btn-edit').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const ma = btn.getAttribute('data-ma');
                // Lấy chi tiết đề thi từ API
                fetch(`/api/de-thi/${ma}/chi-tiet`)
                    .then(res => {
                        if (!res.ok) throw new Error('Không tìm thấy đề thi');
                        return res.json();
                    })
                    .then(data => {
                        // Lưu dữ liệu vào sessionStorage
                        sessionStorage.setItem('deThiMa', ma);
                        if (data.rawText) {
                            sessionStorage.setItem('importedRawText', data.rawText);
                        }
                        if (data.tenMonHoc) {
                            sessionStorage.setItem('importedMonHoc', data.tenMonHoc);
                        }
                        // Chuyển sang trang sửa
                        window.location.href = '/de-thi/sua';
                    })
                    .catch(err => {
                        console.error('Lỗi load đề thi:', err);
                        alert('Không thể tải nội dung đề thi: ' + err.message);
                    });
            });
        });

        // === Nút Xóa ===
        document.querySelectorAll('.btn-delete').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const ma = btn.getAttribute('data-ma');
                if (!confirm('Bạn có chắc muốn xóa đề thi này? Thao tác không thể hoàn tác.')) return;

                fetch(`/api/de-thi/${ma}`, { method: 'DELETE' })
                    .then(res => {
                        if (!res.ok) throw new Error('Lỗi xóa');
                        return res.json();
                    })
                    .then(() => {
                        alert('✅ Đã xóa đề thi thành công!');
                        loadThongKe();
                        loadDanhSach();
                    })
                    .catch(err => {
                        console.error('Lỗi xóa đề thi:', err);
                        alert('❌ Không thể xóa đề thi: ' + err.message);
                    });
            });
        });
    }

    // === 4. Render phân trang ===
    function renderPagination(pageData) {
        const totalElements = pageData.totalElements || 0;
        const totalPages    = pageData.totalPages || 0;
        const currentP      = pageData.number || 0;
        const pageS         = pageData.size || pageSize;

        // Thông tin hiển thị
        const start = totalElements === 0 ? 0 : (currentP * pageS + 1);
        const end   = Math.min((currentP + 1) * pageS, totalElements);
        pageInfo.textContent = `Hiển thị ${start}-${end} trong số ${totalElements} đề thi`;

        // Buttons
        let btnsHtml = '';

        // Nút Prev
        btnsHtml += `<button class="page-btn" data-page="prev" ${currentP === 0 ? 'disabled' : ''}>
            <i class="fa-solid fa-chevron-left"></i>
        </button>`;

        // Các nút số trang (tối đa 5 trang)
        const maxVisible = 5;
        let startPage = Math.max(0, currentP - Math.floor(maxVisible / 2));
        let endPage   = Math.min(totalPages, startPage + maxVisible);
        if (endPage - startPage < maxVisible) {
            startPage = Math.max(0, endPage - maxVisible);
        }

        for (let i = startPage; i < endPage; i++) {
            btnsHtml += `<button class="page-btn ${i === currentP ? 'active' : ''}" data-page="${i}">${i + 1}</button>`;
        }

        // Nút Next
        btnsHtml += `<button class="page-btn" data-page="next" ${currentP >= totalPages - 1 ? 'disabled' : ''}>
            <i class="fa-solid fa-chevron-right"></i>
        </button>`;

        pageControls.innerHTML = btnsHtml;

        // Sự kiện click phân trang
        pageControls.querySelectorAll('.page-btn').forEach(btn => {
            btn.addEventListener('click', function () {
                const pageVal = this.getAttribute('data-page');
                if (pageVal === 'prev' && currentPage > 0) {
                    currentPage--;
                } else if (pageVal === 'next' && currentPage < totalPages - 1) {
                    currentPage++;
                } else if (pageVal !== 'prev' && pageVal !== 'next') {
                    currentPage = parseInt(pageVal);
                }
                loadDanhSach();
            });
        });
    }

    // === 5. Tab click ===
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            currentTab  = tab.getAttribute('data-tab');
            currentPage = 0; // Reset về trang đầu
            loadDanhSach();
        });
    });

    // === 6. Search ===
    if (searchInput) {
        let debounceTimer;
        searchInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentKeyword = searchInput.value;
                currentPage = 0;
                loadDanhSach();
            }, 400);
        });

        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                clearTimeout(debounceTimer);
                currentKeyword = searchInput.value;
                currentPage = 0;
                loadDanhSach();
            }
        });
    }

    // === 7. Filter placeholder ===
    if (btnFilter) {
        btnFilter.addEventListener('click', () => {
            console.log('Mở modal bộ lọc nâng cao');
        });
    }

    // === 8. Đóng dropdown khi click bên ngoài ===
    document.addEventListener('click', () => {
        document.querySelectorAll('.action-dropdown.show').forEach(d => d.classList.remove('show'));
    });

    // === Helper ===
    function escapeHtml(unsafe) {
        if (!unsafe) return '';
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // === Initialize ===
    loadThongKe();
    loadDanhSach();

});

