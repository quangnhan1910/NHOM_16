/**
 * Ngân hàng Câu hỏi - Frontend JS
 * Gọi REST API và render dữ liệu động.
 */
document.addEventListener('DOMContentLoaded', function () {

    // === Config ===
    const API_BASE = '/api/ngan-hang-cau-hoi';
    const PAGE_SIZE = 10;

    // === State ===
    let currentPage = 0;
    let currentMonHoc = '';
    let currentLoaiCauHoi = '';
    let currentMucDoKho = '';
    let currentTuKhoa = '';

    // === DOM Elements ===
    const tableBody = document.getElementById('questionTableBody');
    const emptyState = document.getElementById('emptyState');
    const paginationInfo = document.getElementById('paginationInfo');
    const paginationControls = document.getElementById('paginationControls');
    const paginationContainer = document.getElementById('paginationContainer');
    const searchInput = document.getElementById('searchInput');
    const filterLoai = document.getElementById('filterLoaiCauHoi');
    const filterMucDo = document.getElementById('filterMucDoKho');
    const subjectTabs = document.getElementById('subjectTabs');

    // === Mapping hiển thị ===
    const LOAI_CAU_HOI_MAP = {
        'TRAC_NGHIEM_1_DAP_AN': { label: 'Một đáp án', cssClass: 'type-single' },
        'TRAC_NGHIEM_NHIEU_DAP_AN': { label: 'Nhiều đáp án', cssClass: 'type-multi' },
        'DIEN_DAP_AN': { label: 'Điền Khuyết', cssClass: 'type-fill' }
    };

    const MUC_DO_KHO_MAP = {
        'DE': { label: 'Dễ', cssClass: 'diff-easy' },
        'TRUNG_BINH': { label: 'Trung bình', cssClass: 'diff-medium' },
        'KHO': { label: 'Khó', cssClass: 'diff-hard' }
    };

    // === Fetch dữ liệu từ API ===
    function fetchCauHoi() {
        const params = new URLSearchParams();
        params.append('page', currentPage);
        params.append('size', PAGE_SIZE);

        if (currentMonHoc) params.append('monHoc', currentMonHoc);
        if (currentLoaiCauHoi) params.append('loaiCauHoi', currentLoaiCauHoi);
        if (currentMucDoKho) params.append('mucDoKho', currentMucDoKho);
        if (currentTuKhoa) params.append('tuKhoa', currentTuKhoa);

        fetch(`${API_BASE}?${params.toString()}`)
            .then(response => {
                if (!response.ok) throw new Error('Lỗi khi tải dữ liệu');
                return response.json();
            })
            .then(data => {
                renderTable(data.content);
                renderPagination(data);
            })
            .catch(error => {
                console.error('Lỗi:', error);
                tableBody.innerHTML = '';
                emptyState.style.display = 'block';
                paginationContainer.style.display = 'none';
            });
    }

    // === Render table rows ===
    function renderTable(cauHois) {
        if (!cauHois || cauHois.length === 0) {
            tableBody.innerHTML = '';
            emptyState.style.display = 'block';
            paginationContainer.style.display = 'none';
            return;
        }

        emptyState.style.display = 'none';
        paginationContainer.style.display = 'flex';

        tableBody.innerHTML = cauHois.map(ch => {
            const loai = LOAI_CAU_HOI_MAP[ch.loaiCauHoi] || { label: ch.loaiCauHoi, cssClass: '' };
            const mucDo = MUC_DO_KHO_MAP[ch.mucDoKho] || { label: ch.mucDoKho, cssClass: '' };
            const diem = ch.diem != null ? parseFloat(ch.diem).toFixed(1) : '—';

            return `
                <tr>
                    <td class="col-content">${escapeHtml(ch.noiDung || '')}</td>
                    <td class="col-subject">${escapeHtml(ch.tenMonHoc || '')}</td>
                    <td><span class="badge ${loai.cssClass}">${loai.label}</span></td>
                    <td><span class="badge badge-rounded ${mucDo.cssClass}">${mucDo.label}</span></td>
                    <td class="col-score">${diem}</td>
                    <td class="col-actions">
                        <button class="action-btn edit" title="Sửa" onclick="editCauHoi(${ch.ma})">
                            <i class="fa-solid fa-pen"></i>
                        </button>
                        <button class="action-btn delete" title="Xóa" onclick="deleteCauHoi(${ch.ma})">
                            <i class="fa-regular fa-trash-can"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    // === Render pagination ===
    function renderPagination(pageData) {
        const totalPages = pageData.totalPages;
        const totalElements = pageData.totalElements;
        const pageNumber = pageData.number;
        const pageSize = pageData.size;
        const start = pageNumber * pageSize + 1;
        const end = Math.min(start + pageSize - 1, totalElements);

        paginationInfo.textContent = `Hiển thị ${start}-${end} trong số ${totalElements} câu hỏi`;

        let html = '';

        // Nút Previous
        html += `<a href="#" class="page-btn page-nav ${pageNumber === 0 ? 'disabled' : ''}" 
                    onclick="goToPage(${pageNumber - 1}); return false;">
                    <i class="fa-solid fa-chevron-left"></i>
                 </a>`;

        // Các nút trang
        const pages = getVisiblePages(pageNumber, totalPages);
        pages.forEach(p => {
            if (p === '...') {
                html += '<span class="page-btn dots">...</span>';
            } else {
                html += `<a href="#" class="page-btn ${p === pageNumber ? 'active' : ''}" 
                            onclick="goToPage(${p}); return false;">${p + 1}</a>`;
            }
        });

        // Nút Next
        html += `<a href="#" class="page-btn page-nav ${pageNumber >= totalPages - 1 ? 'disabled' : ''}" 
                    onclick="goToPage(${pageNumber + 1}); return false;">
                    <i class="fa-solid fa-chevron-right"></i>
                 </a>`;

        paginationControls.innerHTML = html;
    }

    // === Tính toán các trang hiển thị ===
    function getVisiblePages(current, total) {
        if (total <= 5) {
            return Array.from({ length: total }, (_, i) => i);
        }
        const pages = [];
        pages.push(0);
        if (current > 2) pages.push('...');
        for (let i = Math.max(1, current - 1); i <= Math.min(total - 2, current + 1); i++) {
            pages.push(i);
        }
        if (current < total - 3) pages.push('...');
        pages.push(total - 1);
        return pages;
    }

    // === Escape HTML để tránh XSS ===
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.appendChild(document.createTextNode(text));
        return div.innerHTML;
    }

    // === Event Handlers ===

    // Tabs môn học
    subjectTabs.addEventListener('click', function (e) {
        const tab = e.target.closest('.nav-tab');
        if (!tab) return;

        subjectTabs.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');

        currentMonHoc = tab.dataset.monHoc || '';
        currentPage = 0;
        fetchCauHoi();
    });

    // Filter loại câu hỏi
    filterLoai.addEventListener('change', function () {
        currentLoaiCauHoi = this.value;
        currentPage = 0;
        fetchCauHoi();
    });

    // Filter mức độ
    filterMucDo.addEventListener('change', function () {
        currentMucDoKho = this.value;
        currentPage = 0;
        fetchCauHoi();
    });

    // Tìm kiếm (nhấn Enter)
    let searchTimeout;
    searchInput.addEventListener('input', function () {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            currentTuKhoa = this.value.trim();
            currentPage = 0;
            fetchCauHoi();
        }, 400); // Debounce 400ms
    });

    // === Global functions cho onclick ===
    window.goToPage = function (page) {
        if (page < 0) return;
        currentPage = page;
        fetchCauHoi();
    };

    window.editCauHoi = function (ma) {
        // TODO: Chuyển đến trang sửa câu hỏi
        console.log('Sửa câu hỏi mã:', ma);
        alert('Chức năng sửa câu hỏi đang phát triển (mã: ' + ma + ')');
    };

    window.deleteCauHoi = function (ma) {
        if (!confirm('Bạn có chắc chắn muốn xóa câu hỏi này không?')) return;

        fetch(`${API_BASE}/${ma}`, { method: 'DELETE' })
            .then(response => {
                if (!response.ok) throw new Error('Lỗi khi xóa');
                return response.json();
            })
            .then(() => {
                fetchCauHoi(); // Reload danh sách
            })
            .catch(error => {
                console.error('Lỗi xóa:', error);
                alert('Xóa câu hỏi thất bại!');
            });
    };

    // === Load danh sách Môn học cho Tabs ===
    function loadMonHocTabs() {
        fetch(`${API_BASE}/mon-hoc`)
            .then(response => {
                if (!response.ok) throw new Error('Lỗi khi tải danh sách môn học');
                return response.json();
            })
            .then(monHocs => {
                // Giữ tab "Tất cả" đã có sẵn, thêm các tab môn học mới
                monHocs.forEach(mh => {
                    const btn = document.createElement('button');
                    btn.className = 'nav-tab';
                    btn.dataset.monHoc = mh.ma;
                    btn.textContent = mh.ten;
                    subjectTabs.appendChild(btn);
                });
            })
            .catch(error => {
                console.error('Lỗi tải môn học:', error);
            });
    }

    // === Khởi tạo ===
    loadMonHocTabs();
    fetchCauHoi();
});
