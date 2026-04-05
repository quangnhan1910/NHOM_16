/**
 * JavaScript for Quan ly Co cau To chuc page.
 * API: /api/truong, /api/khoa, /api/nganh, /api/chuyen-nganh
 */
(function () {
    'use strict';

    // ===================== State =====================
    let currentTab = 'all';
    let expandedNodes = new Set();
    let defaultExpanded = true;
    let allTruongs = [];
    let allKhoas = [];
    let allNganhs = [];
    let allChuyenNganhs = [];

    var TREE_STATE_KEY = 'coCauToChuc_treeState';
    var searchSuggestTimer = null;
    var treeStateInitialRestored = false;
    var saveTreeStateTimer = null;

    // ===================== DOM Elements =====================
    const treeView = document.getElementById('treeView');
    const editorPanel = document.getElementById('editorPanel');
    const editorPanelTitle = document.getElementById('editorPanelTitle');
    const btnCloseEditor = document.getElementById('btnCloseEditor');
    const btnCancelEdit = document.getElementById('btnCancelEdit');
    const btnSaveEdit = document.getElementById('btnSaveEdit');
    const btnExpandAll = document.getElementById('btnExpandAll');
    const btnCollapseAll = document.getElementById('btnCollapseAll');
    const btnThemTruong = document.getElementById('btnThemTruong');
    const searchInput = document.getElementById('searchInput');
    const searchSuggestions = document.getElementById('searchSuggestions');
    const tabs = document.querySelectorAll('.filter-tab');
    const formDonVi = document.getElementById('formDonVi');

    // ===================== API Helper =====================
    function api(url, opts) {
        return fetch(url, {
            headers: { 'Content-Type': 'application/json' },
            ...opts
        }).then(function (r) {
            if (!r.ok) {
                return r.json().then(function (body) {
                    var msg = (body && body.message) || ('HTTP ' + r.status);
                    throw new Error(msg);
                });
            }
            return r.json();
        });
    }

    function apiOk(url, opts) {
        return api(url, opts).then(function (res) {
            if (!res.success) throw new Error(res.message || 'Thao tác thất bại');
            return res;
        });
    }

    // ===================== Toast =====================
    function showToast(msg, isError) {
        var existing = document.querySelector('.toast');
        if (existing) existing.remove();
        var el = document.createElement('div');
        el.className = 'toast ' + (isError ? 'error' : 'success');
        el.textContent = msg;
        document.body.appendChild(el);
        setTimeout(function () { el.remove(); }, 3500);
    }

    // ===================== Form Control =====================
    function formSectionIdMap() {
        return { TRUONG: 'formTruong', KHOA: 'formKhoa', NGANH: 'formNganh', CHUYEN_NGANH: 'formChuyenNganh' };
    }

    function showFormSection(level) {
        var map = formSectionIdMap();
        Object.values(map).forEach(function (id) {
            var el = document.getElementById(id);
            if (el) el.style.display = 'none';
        });
        var target = map[level];
        if (target) {
            var el = document.getElementById(target);
            if (el) el.style.display = 'block';
        }
    }

    function clearForm() {
        // Clear all Truong fields
        document.getElementById('fTruong_ten').value = '';
        document.getElementById('fTruong_capBac').value = '';
        document.getElementById('fTruong_maDinhDanh').value = '';
        document.getElementById('fTruong_diaChi').value = '';

        // Reset Khoa batch list to 1 empty row
        var khoaList = document.getElementById('fTruong_khoaBatchList');
        if (khoaList) khoaList.innerHTML = singleBatchRow('khoa-name');

        // Clear Khoa fields
        document.getElementById('fKhoa_ten').value = '';
        document.getElementById('fKhoa_maTruong').value = '';

        // Reset Nganh batch list to 1 empty row (Form 2: Thêm nhiều ngành theo khoa)
        var nganhKhoaList = document.getElementById('fKhoa_nganhBatchList');
        if (nganhKhoaList) nganhKhoaList.innerHTML = singleBatchRow('nganh-name');
        var nganhBatchList = document.getElementById('fNganh_nganhBatchList');
        if (nganhBatchList) nganhBatchList.innerHTML = singleBatchRow('nganh-name');

        // Clear Nganh fields
        document.getElementById('fNganh_ten').value = '';
        document.getElementById('fNganh_maKhoa').value = '';

        // Reset CN batch list to 1 empty row
        var cnNganhList = document.getElementById('fNganh_cnBatchList');
        if (cnNganhList) cnNganhList.innerHTML = singleBatchRow('cn-name');

        // Clear CN fields
        document.getElementById('fCN_ten').value = '';
        document.getElementById('fCN_maNganh').value = '';

        // Hidden fields
        document.getElementById('f_level').value = 'TRUONG';
        document.getElementById('f_editId').value = '';
        document.getElementById('f_editMode').value = 'add';

        // Clear error states
        document.querySelectorAll('.form-group.has-error').forEach(function (g) {
            g.classList.remove('has-error');
        });
    }

    function closeEditorPanel() {
        editorPanel.style.display = 'none';
        clearForm();
        setFormInputsDisabled(false);
        btnSaveEdit.style.display = '';
        btnCancelEdit.textContent = 'Hủy bỏ';
    }

    function showEditorPanel() {
        editorPanel.style.display = '';
        requestAnimationFrame(function () {
            requestAnimationFrame(function () {
                try {
                    editorPanel.scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
                } catch (e) {
                    editorPanel.scrollIntoView(true);
                }
            });
        });
    }

    function setFormInputsDisabled(disabled) {
        var form = document.getElementById('formDonVi');
        if (!form) return;
        form.querySelectorAll('input, select, textarea').forEach(function (input) {
            if (input.type !== 'hidden') {
                input.disabled = disabled;
            }
        });
    }

    // ===================== Batch Row Helpers =====================
    function singleBatchRow(cls) {
        return '<div class="batch-item-row">' +
            '<input type="text" class="form-input ' + cls + '" placeholder="vd: Tên mới">' +
            '<button type="button" class="btn-remove" onclick="removeBatchItem(this)">×</button>' +
            '</div>';
    }

    window.addKhoaBatch = function () {
        var list = document.getElementById('fTruong_khoaBatchList');
        if (list) list.insertAdjacentHTML('beforeend', singleBatchRow('khoa-name'));
    };

    window.addNganhBatch = function () {
        var list = document.getElementById('fKhoa_nganhBatchList');
        if (list) list.insertAdjacentHTML('beforeend', singleBatchRow('nganh-name'));
    };

    window.addCnBatch = function () {
        var list = document.getElementById('fNganh_cnBatchList');
        if (list) list.insertAdjacentHTML('beforeend', singleBatchRow('cn-name'));
    };

    window.removeBatchItem = function (btn) {
        var row = btn.closest('.batch-item-row');
        var list = row.parentElement;
        if (list.children.length > 1) {
            row.remove();
        } else {
            row.querySelector('input').value = '';
        }
    };

    // ===================== Lưu / khôi phục trạng thái cây (tab + nút mở rộng) =====================
    function saveTreeState() {
        try {
            sessionStorage.setItem(TREE_STATE_KEY, JSON.stringify({
                expanded: Array.from(expandedNodes),
                tab: currentTab
            }));
        } catch (e) { /* ignore */ }
    }

    function saveTreeStateDebounced() {
        clearTimeout(saveTreeStateTimer);
        saveTreeStateTimer = setTimeout(saveTreeState, 120);
    }

    /** restoreTab: true = khôi phục cả tab (chỉ dùng lần đầu vào trang, tránh ghi đè khi user đổi tab). */
    function loadTreeStateFromStorage(restoreTab) {
        try {
            var raw = sessionStorage.getItem(TREE_STATE_KEY);
            if (!raw) return;
            var o = JSON.parse(raw);
            if (o.expanded && Array.isArray(o.expanded)) {
                expandedNodes = new Set(o.expanded);
            }
            if (restoreTab && o.tab && typeof o.tab === 'string') {
                currentTab = o.tab;
            }
        } catch (e) { /* ignore */ }
    }

    function syncTabUI() {
        tabs.forEach(function (t) {
            t.classList.toggle('active', t.dataset.tab === currentTab);
        });
    }

    // ===================== Load Data =====================
    function loadAll() {
        Promise.all([
            api('/api/truong/tat-ca'),
            api('/api/khoa/tat-ca'),
            api('/api/nganh/tat-ca'),
            api('/api/chuyen-nganh/tat-ca')
        ]).then(function (results) {
            allTruongs = (results[0].data || []).map(toLower);
            allKhoas = (results[1].data || []).map(toLower);
            allNganhs = (results[2].data || []).map(toLower);
            allChuyenNganhs = (results[3].data || []).map(toLower);
            if (!treeStateInitialRestored) {
                loadTreeStateFromStorage(true);
                treeStateInitialRestored = true;
            }
            syncTabUI();
            renderTree();
        }).catch(function (e) {
            treeView.innerHTML = '<div class="empty-tree">Lỗi tải dữ liệu: ' + e.message + '</div>';
        });
    }

    function toLower(item) {
        var out = {}, k;
        for (k in item) out[k.toLowerCase()] = item[k];
        return out;
    }

    // ===================== Tree Render =====================
    function renderTree() {
        var keyword = (searchInput.value || '').toLowerCase().trim();
        var html = '';

        if (currentTab === 'all' || currentTab === 'TRUONG') {
            allTruongs.forEach(function (truong) {
                if (keyword && !matchKeyword(truong, keyword)) return;
                html += buildTruongNode(truong);
            });
        }

        if (currentTab === 'KHOA') {
            allKhoas.forEach(function (khoa) {
                if (keyword && !matchKeyword(khoa, keyword)) return;
                html += buildKhoaNode(khoa, true);
            });
        }

        if (currentTab === 'NGANH') {
            allNganhs.forEach(function (nganh) {
                if (keyword && !matchKeyword(nganh, keyword)) return;
                html += buildNganhNode(nganh, currentTab === 'NGANH');
            });

            allChuyenNganhs.forEach(function (cn) {
                if (keyword && !matchKeyword(cn, keyword)) return;
                html += buildChuyenNganhNode(cn, currentTab === 'NGANH');
            });
        }

        if (!html) {
            html = '<div class="empty-tree">Không có dữ liệu phù hợp.</div>';
        }

        treeView.innerHTML = html;
        bindTreeActions();
        saveTreeStateDebounced();
    }

    function matchKeyword(item, kw) {
        return (item.ten && item.ten.toLowerCase().includes(kw)) ||
               (item.madinhdanh && item.madinhdanh.toLowerCase().includes(kw));
    }

    // ===================== Gợi ý tìm kiếm (xếp: khớp chính xác → đầu dòng → từ → chứa → gần đúng) =====================
    function normalizeSearch(s) {
        if (s == null) return '';
        return String(s).toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
    }

    function scoreMatch(haystack, query) {
        var t = normalizeSearch(haystack);
        var q = normalizeSearch(query);
        if (!q || !t) return 0;
        if (t === q) return 100000;
        if (t.startsWith(q)) return 80000 + Math.min(5000, q.length * 200);
        var words = t.split(/\s+/);
        var i;
        for (i = 0; i < words.length; i++) {
            if (words[i].startsWith(q)) return 70000;
        }
        var pos = t.indexOf(q);
        if (pos >= 0) return 60000 - Math.min(pos, 500);
        var si = 0;
        for (i = 0; i < q.length; i++) {
            var ix = t.indexOf(q[i], si);
            if (ix < 0) return 0;
            si = ix + 1;
        }
        return 40000 - Math.min(t.length, 200);
    }

    function collectRankedSearch(query) {
        if (!query || String(query).trim().length < 1) return [];
        var out = [];
        function pushItem(item, level, label, textFields) {
            var best = 0;
            var bestText = '';
            textFields.forEach(function (tf) {
                if (!tf) return;
                var s = scoreMatch(tf, query);
                if (s > best) {
                    best = s;
                    bestText = tf;
                }
            });
            if (best > 0) {
                out.push({ score: best, item: item, level: level, label: label, text: bestText || textFields[0] || '' });
            }
        }
        allTruongs.forEach(function (t) {
            pushItem(t, 'TRUONG', 'Trường', [t.ten, t.madinhdanh]);
        });
        allKhoas.forEach(function (k) {
            pushItem(k, 'KHOA', 'Khoa', [k.ten]);
        });
        allNganhs.forEach(function (n) {
            pushItem(n, 'NGANH', 'Ngành', [n.ten]);
        });
        allChuyenNganhs.forEach(function (c) {
            pushItem(c, 'CN', 'Chuyên ngành', [c.ten]);
        });
        out.sort(function (a, b) { return b.score - a.score; });
        return out.slice(0, 15);
    }

    function hideSearchSuggestions() {
        if (!searchSuggestions) return;
        searchSuggestions.style.display = 'none';
        searchSuggestions.innerHTML = '';
    }

    function renderSearchSuggestions(ranked) {
        if (!searchSuggestions) return;
        searchSuggestions.innerHTML = '';
        ranked.forEach(function (c) {
            var div = document.createElement('div');
            div.className = 'search-suggest-item';
            div.innerHTML =
                '<span class="search-suggest-type">' + escHtml(c.label) + '</span>' +
                '<span class="search-suggest-name">' + escHtml(c.text) + '</span>';
            div.addEventListener('mousedown', function (e) {
                e.preventDefault();
                searchInput.value = c.text;
                expandAncestorsForCandidate(c);
            });
            searchSuggestions.appendChild(div);
        });
        searchSuggestions.style.display = 'block';
    }

    function expandAncestorsForCandidate(c) {
        var item = c.item;
        if (c.level === 'TRUONG') {
            expandedNodes.add('t_' + item.ma);
        } else if (c.level === 'KHOA') {
            expandedNodes.add('t_' + item.matruong);
            expandedNodes.add('k_' + item.ma);
        } else if (c.level === 'NGANH') {
            var k = findKhoa(item.makhoa);
            if (k) {
                expandedNodes.add('t_' + k.matruong);
                expandedNodes.add('k_' + k.ma);
            }
            expandedNodes.add('n_' + item.ma);
        } else if (c.level === 'CN') {
            var n = findNganh(item.manganh);
            if (n) {
                var k2 = findKhoa(n.makhoa);
                if (k2) {
                    expandedNodes.add('t_' + k2.matruong);
                    expandedNodes.add('k_' + k2.ma);
                }
                expandedNodes.add('n_' + n.ma);
            }
        }
        currentTab = 'all';
        syncTabUI();
        hideSearchSuggestions();
        renderTree();
    }

    function scheduleSearchSuggestions() {
        if (!searchSuggestions) return;
        clearTimeout(searchSuggestTimer);
        searchSuggestTimer = setTimeout(function () {
            var q = (searchInput.value || '').trim();
            if (q.length < 1) {
                hideSearchSuggestions();
                return;
            }
            var ranked = collectRankedSearch(q);
            if (!ranked.length) {
                searchSuggestions.innerHTML = '<div class="search-suggest-empty">Không có gợi ý phù hợp</div>';
                searchSuggestions.style.display = 'block';
                return;
            }
            renderSearchSuggestions(ranked);
        }, 180);
    }

    function getCapBacLabel(val) {
        if (!val) return '';
        var map = { DAI_HOC: 'Đại học', CAO_DANG: 'Cao đẳng', TRUNG_CAP: 'Trung cấp' };
        return map[val.toUpperCase()] || val;
    }

    // ===================== Node Builders =====================
    function buildTruongNode(truong) {
        var ma = truong.ma;
        var ten = truong.ten || '';
        var madinhdanh = truong.madinhdanh || '';
        var capbac = truong.capbac || '';
        var diachi = truong.diachi || '';
        var isExp = expandedNodes.has('t_' + ma);
        var children = getKhoasByTruong(ma);

        var khoaChildren = '';
        if (isExp) {
            children.forEach(function (k) {
                khoaChildren += buildKhoaChild(k);
            });
        }

        return '<div class="tree-item">' +
            '<div class="tree-row" data-type="truong" data-id="' + ma + '">' +
                buildExpandIcon(isExp, 't_' + ma) +
                buildTypeIcon('TRUONG') +
                '<div class="tree-info">' +
                    '<div class="tree-name">' + escHtml(ten) +
                        (madinhdanh ? '<span class="tree-code">' + escHtml(madinhdanh) + '</span>' : '') +
                        (capbac ? '<span class="tree-code small">' + escHtml(getCapBacLabel(capbac)) + '</span>' : '') +
                    '</div>' +
                '</div>' +
                buildTreeActions('TRUONG', ma, isExp) +
            '</div>' +
            (isExp ? '<div class="tree-children">' + khoaChildren + '</div>' : '') +
        '</div>';
    }

    function buildKhoaChild(khoa) {
        var ma = khoa.ma;
        var ten = khoa.ten || '';
        var isExp = expandedNodes.has('k_' + ma);
        var nganhChildren = '';

        if (isExp) {
            getNganhsByKhoa(ma).forEach(function (n) {
                nganhChildren += buildNganhChild(n);
            });
        }

        return '<div class="tree-item">' +
            '<div class="tree-child-row" data-type="khoa" data-id="' + ma + '">' +
                buildExpandIcon(isExp, 'k_' + ma, true) +
                buildTypeIcon('KHOA', true) +
                '<div class="tree-child-info">' +
                    '<div class="tree-name medium">' + escHtml(ten) + '</div>' +
                '</div>' +
                buildTreeActions('KHOA', ma, isExp) +
            '</div>' +
            (isExp ? '<div class="tree-children">' + nganhChildren + '</div>' : '') +
        '</div>';
    }

    function buildKhoaNode(khoa) {
        // Standalone Khoa row (when tab = KHOA)
        var ma = khoa.ma;
        var ten = khoa.ten || '';
        var isExp = expandedNodes.has('k_' + ma);
        var nganhChildren = '';

        if (isExp) {
            getNganhsByKhoa(ma).forEach(function (n) {
                nganhChildren += buildNganhChild(n);
            });
        }

        return '<div class="tree-item">' +
            '<div class="tree-row" data-type="khoa" data-id="' + ma + '">' +
                buildExpandIcon(isExp, 'k_' + ma) +
                buildTypeIcon('KHOA') +
                '<div class="tree-info">' +
                    '<div class="tree-name medium">' + escHtml(ten) + '</div>' +
                '</div>' +
                buildTreeActions('KHOA', ma, isExp) +
            '</div>' +
            (isExp ? '<div class="tree-children">' + nganhChildren + '</div>' : '') +
        '</div>';
    }

    function buildNganhChild(nganh) {
        var ma = nganh.ma;
        var ten = nganh.ten || '';
        var isExp = expandedNodes.has('n_' + ma);
        var cnChildren = '';

        if (isExp) {
            getChuyenNganhsByNganh(ma).forEach(function (cn) {
                cnChildren += buildChuyenNganhChild(cn);
            });
        }

        return '<div class="tree-item">' +
            '<div class="tree-child-row" data-type="nganh" data-id="' + ma + '">' +
                buildExpandIcon(isExp, 'n_' + ma, true) +
                buildTypeIcon('NGANH', true) +
                '<div class="tree-child-info">' +
                    '<div class="tree-name medium">' + escHtml(ten) + '</div>' +
                '</div>' +
                buildTreeActions('NGANH', ma, isExp) +
            '</div>' +
            (isExp ? '<div class="tree-children">' + cnChildren + '</div>' : '') +
        '</div>';
    }

    function buildNganhNode(nganh, standalone) {
        var ma = nganh.ma;
        var ten = nganh.ten || '';
        var isExp = expandedNodes.has('n_' + ma);
        var cnChildren = '';

        if (isExp) {
            getChuyenNganhsByNganh(ma).forEach(function (cn) {
                cnChildren += buildChuyenNganhChild(cn);
            });
        }

        var cls = standalone ? 'tree-row' : 'tree-child-row';
        var iconCls = standalone ? '' : 'small';

        return '<div class="tree-item">' +
            '<div class="' + cls + '" data-type="nganh" data-id="' + ma + '">' +
                buildExpandIcon(isExp, 'n_' + ma, !standalone) +
                buildTypeIcon('NGANH', !standalone, iconCls) +
                '<div class="tree-child-info">' +
                    '<div class="tree-name medium">' + escHtml(ten) + '</div>' +
                '</div>' +
                buildTreeActions('NGANH', ma, isExp) +
            '</div>' +
            (isExp ? '<div class="tree-children">' + cnChildren + '</div>' : '') +
        '</div>';
    }

    function buildChuyenNganhChild(cn) {
        var ma = cn.ma;
        var ten = cn.ten || '';

        return '<div class="tree-item">' +
            '<div class="tree-level3" data-type="chuyennganh" data-id="' + ma + '">' +
                '<div class="tree-level3-left">' +
                    '<div class="tree-level3-type">CN</div>' +
                    '<div class="tree-name small">' + escHtml(ten) + '</div>' +
                '</div>' +
                buildTreeActions('CHUYEN_NGANH', ma, false) +
            '</div>' +
        '</div>';
    }

    function buildChuyenNganhNode(cn, standalone) {
        var ma = cn.ma;
        var ten = cn.ten || '';

        var cls = standalone ? 'tree-row' : 'tree-level3';
        var iconCls = standalone ? '' : 'smaller';

        return '<div class="tree-item">' +
            '<div class="' + cls + '" data-type="chuyennganh" data-id="' + ma + '">' +
                (standalone ? buildExpandIcon(false, '', false) + buildTypeIcon('CHUYEN_NGANH', false, '') : '') +
                (!standalone ? '<div class="tree-level3-left">' : '') +
                    (!standalone ? '<div class="tree-level3-type">CN</div>' : '') +
                    '<div class="tree-name small">' + escHtml(ten) + '</div>' +
                (!standalone ? '</div>' : '') +
                buildTreeActions('CHUYEN_NGANH', ma, false) +
            '</div>' +
        '</div>';
    }

    // ===================== Icons (mở rộng: mũi tên phải / xuống — chuẩn tree) =====================
    function buildExpandIcon(isExp, nodeKey, isChild) {
        var cls = isChild ? 'tree-child-expand' : 'tree-expand-icon';
        if (!nodeKey) {
            return '<span style="width:12px;display:inline-block;flex-shrink:0"></span>';
        }
        if (isExp) {
            return '<svg class="' + cls + '" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg" data-action="toggle" data-key="' + nodeKey + '" aria-expanded="true">' +
                '<path d="M2.5 4.5L6 8L9.5 4.5" stroke="#64748B" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>' +
                '</svg>';
        }
        return '<svg class="' + cls + ' collapsed" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg" data-action="toggle" data-key="' + nodeKey + '" aria-expanded="false">' +
            '<path d="M4.5 2.5L8 6L4.5 9.5" stroke="#64748B" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>' +
            '</svg>';
    }

    function buildTypeIcon(type, isChild, sizeClass) {
        var cls = (isChild ? 'tree-type-icon ' : 'tree-type-icon ');
        if (sizeClass) cls += sizeClass + ' ';
        if (type === 'TRUONG') {
            return '<svg class="' + cls + '" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                '<path d="M10 2L18 7V13L10 18L2 13V7L10 2Z" stroke="#1C74E9" stroke-width="1.5" stroke-linejoin="round"/>' +
                '<path d="M10 7V13M10 13L2 7M10 13L18 7" stroke="#1C74E9" stroke-width="1.5"/>' +
                '</svg>';
        } else if (type === 'KHOA') {
            return '<svg class="' + cls + '" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                '<rect x="3" y="4" width="14" height="12" rx="1" stroke="#10B981" stroke-width="1.5"/>' +
                '<path d="M7 4V16M13 4V16" stroke="#10B981" stroke-width="1.5"/>' +
                '<path d="M3 8H17" stroke="#10B981" stroke-width="1.5"/>' +
                '</svg>';
        } else if (type === 'NGANH') {
            return '<svg class="' + cls + '" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                '<path d="M4 6H16M4 10H12M4 14H14" stroke="#F59E0B" stroke-width="1.5" stroke-linecap="round"/>' +
                '</svg>';
        } else {
            return '<svg class="' + cls + '" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">' +
                '<circle cx="10" cy="10" r="7" stroke="#8B5CF6" stroke-width="1.5"/>' +
                '<path d="M10 6V10L13 13" stroke="#8B5CF6" stroke-width="1.5" stroke-linecap="round"/>' +
                '</svg>';
        }
    }

    function buildTreeActions(type, id, hasChildren) {
        var addChildBtn = '';
        if (type === 'TRUONG') {
            addChildBtn = '<span class="tree-action-icon" data-action="add-child" data-type="KHOA" data-parent="' + id + '" title="Thêm khoa">' +
                '<svg viewBox="0 0 12 12" fill="none"><path d="M6 1V11M1 6H11" stroke="#10B981" stroke-width="1.5" stroke-linecap="round"/></svg>' +
                '</span>';
        } else if (type === 'KHOA') {
            addChildBtn = '<span class="tree-action-icon" data-action="add-child" data-type="NGANH" data-parent="' + id + '" title="Thêm ngành">' +
                '<svg viewBox="0 0 12 12" fill="none"><path d="M6 1V11M1 6H11" stroke="#10B981" stroke-width="1.5" stroke-linecap="round"/></svg>' +
                '</span>';
        } else if (type === 'NGANH') {
            addChildBtn = '<span class="tree-action-icon" data-action="add-child" data-type="CHUYEN_NGANH" data-parent="' + id + '" title="Thêm chuyên ngành">' +
                '<svg viewBox="0 0 12 12" fill="none"><path d="M6 1V11M1 6H11" stroke="#10B981" stroke-width="1.5" stroke-linecap="round"/></svg>' +
                '</span>';
        }

        return '<div class="tree-row-actions">' +
            addChildBtn +
            '<span class="tree-action-icon md" data-action="view-detail" data-type="' + type + '" data-id="' + id + '" title="Chi tiết">' +
                '<svg viewBox="0 0 15 15" fill="none"><path d="M7.5 4.5C5.5 4.5 3.5 6 1.5 9C3.5 12 5.5 13.5 7.5 13.5C9.5 13.5 11.5 12 13.5 9C11.5 6 9.5 4.5 7.5 4.5Z" stroke="#64748B" stroke-width="1.5"/><circle cx="7.5" cy="9" r="2" stroke="#64748B" stroke-width="1.5"/></svg>' +
            '</span>' +
            '<span class="tree-action-icon md" data-action="edit" data-type="' + type + '" data-id="' + id + '" title="Sửa">' +
                '<svg viewBox="0 0 15 15" fill="none"><path d="M10.5 1.5L13.5 4.5L5 13H2V10L10.5 1.5Z" stroke="#64748B" stroke-width="1.5" stroke-linejoin="round"/></svg>' +
            '</span>' +
            '<span class="tree-action-icon" data-action="delete" data-type="' + type + '" data-id="' + id + '" title="Xóa">' +
                '<svg viewBox="0 0 13.5 15" fill="none"><path d="M2 4H12.5M5 4V2.5H8.5V4M5.5 7V12M8 7V12M3 4L4 13.5H9.5L10.5 4" stroke="#64748B" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>' +
            '</span>' +
        '</div>';
    }

    // ===================== Lookup Helpers =====================
    function getKhoasByTruong(maTruong) {
        return allKhoas.filter(function (k) { return k.matruong === maTruong; });
    }

    function getNganhsByKhoa(maKhoa) {
        return allNganhs.filter(function (n) { return n.makhoa === maKhoa; });
    }

    function getChuyenNganhsByNganh(maNganh) {
        return allChuyenNganhs.filter(function (c) { return c.manganh === maNganh; });
    }

    function findTruong(ma) { return allTruongs.find(function (t) { return t.ma === ma; }); }
    function findKhoa(ma) { return allKhoas.find(function (k) { return k.ma === ma; }); }
    function findNganh(ma) { return allNganhs.find(function (n) { return n.ma === ma; }); }
    function findChuyenNganh(ma) { return allChuyenNganhs.find(function (c) { return c.ma === ma; }); }

    // ===================== Escape =====================
    function escHtml(str) {
        if (str == null) return '';
        return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    // ===================== Event: Toggle =====================
    function handleToggle(key) {
        if (expandedNodes.has(key)) {
            expandedNodes.delete(key);
        } else {
            expandedNodes.add(key);
        }
        renderTree();
    }

    // ===================== Event: Actions =====================
    function handleAction(action, type, id, parentId) {
        if (action === 'toggle') {
            handleToggle(id);
        } else if (action === 'add-child') {
            openAddForm(type, parentId);
        } else if (action === 'view-detail') {
            if (type === 'TRUONG') {
                window.location.href = '/admin/co-cau-to-chuc/truong/' + id;
                return;
            }
            openDetailView(type, id);
        } else if (action === 'edit') {
            if (type === 'TRUONG') {
                window.location.href = '/admin/co-cau-to-chuc/truong/' + id + '/sua';
                return;
            }
            openEditForm(type, id);
        } else if (action === 'delete') {
            handleDelete(type, id);
        }
    }

    // ===================== Open Add Form =====================
    function openAddForm(level, parentId) {
        clearForm();
        document.getElementById('f_editMode').value = 'add';

        if (level === 'TRUONG') {
            editorPanelTitle.textContent = 'Thêm trường mới';
            showFormSection('TRUONG');
            document.getElementById('fTruong_khoaList').style.display = '';
        } else if (level === 'KHOA') {
            editorPanelTitle.textContent = 'Thêm khoa mới';
            showFormSection('KHOA');
            loadTruongDropdown();
            document.getElementById('fKhoa_nganhList').style.display = '';
            if (parentId) {
                document.getElementById('fKhoa_maTruong').value = parentId;
            }
        } else if (level === 'NGANH') {
            editorPanelTitle.textContent = 'Thêm ngành mới';
            showFormSection('NGANH');
            loadKhoaDropdown();
            document.getElementById('fNganh_cnList').style.display = '';
            if (parentId) {
                document.getElementById('fNganh_maKhoa').value = parentId;
            }
        } else if (level === 'CHUYEN_NGANH') {
            editorPanelTitle.textContent = 'Thêm chuyên ngành mới';
            showFormSection('CHUYEN_NGANH');
            loadNganhDropdown();
            if (parentId) {
                document.getElementById('fCN_maNganh').value = parentId;
            }
        }

        document.getElementById('f_level').value = level;
        setFormInputsDisabled(false);
        btnSaveEdit.style.display = '';
        btnCancelEdit.textContent = 'Hủy bỏ';
        showEditorPanel();
    }

    // ===================== Open Edit Form =====================
    function openEditForm(level, id) {
        clearForm();
        document.getElementById('f_editMode').value = 'edit';
        document.getElementById('f_editId').value = id;

        if (level === 'TRUONG') {
            var truong = findTruong(id);
            if (!truong) return;
            editorPanelTitle.textContent = 'Sửa trường';
            showFormSection('TRUONG');
            document.getElementById('fTruong_ten').value = truong.ten || '';
            document.getElementById('fTruong_capBac').value = (truong.capbac || '').toUpperCase();
            document.getElementById('fTruong_maDinhDanh').value = truong.madinhdanh || '';
            document.getElementById('fTruong_diaChi').value = truong.diachi || '';
            document.getElementById('fTruong_khoaList').style.display = 'none';
        } else if (level === 'KHOA') {
            var khoa = findKhoa(id);
            if (!khoa) return;
            editorPanelTitle.textContent = 'Sửa khoa';
            showFormSection('KHOA');
            loadTruongDropdown();
            document.getElementById('fKhoa_ten').value = khoa.ten || '';
            document.getElementById('fKhoa_maTruong').value = khoa.matruong || '';
            document.getElementById('fKhoa_nganhList').style.display = 'none';
        } else if (level === 'NGANH') {
            var nganh = findNganh(id);
            if (!nganh) return;
            editorPanelTitle.textContent = 'Sửa ngành';
            showFormSection('NGANH');
            loadKhoaDropdown();
            document.getElementById('fNganh_ten').value = nganh.ten || '';
            document.getElementById('fNganh_maKhoa').value = nganh.makhoa || '';
            document.getElementById('fNganh_cnList').style.display = 'none';
        } else if (level === 'CHUYEN_NGANH') {
            var cn = findChuyenNganh(id);
            if (!cn) return;
            editorPanelTitle.textContent = 'Sửa chuyên ngành';
            showFormSection('CHUYEN_NGANH');
            loadNganhDropdown();
            document.getElementById('fCN_ten').value = cn.ten || '';
            document.getElementById('fCN_maNganh').value = cn.manganh || '';
        }

        document.getElementById('f_level').value = level;
        setFormInputsDisabled(false);
        btnSaveEdit.style.display = '';
        btnCancelEdit.textContent = 'Hủy bỏ';
        showEditorPanel();
    }

    // ===================== Open Detail View =====================
    function openDetailView(level, id) {
        clearForm();
        document.getElementById('f_editMode').value = 'view';
        document.getElementById('f_editId').value = id;

        if (level === 'TRUONG') {
            var truong = findTruong(id);
            if (!truong) return;
            editorPanelTitle.textContent = 'Chi tiết trường';
            showFormSection('TRUONG');
            document.getElementById('fTruong_ten').value = truong.ten || '';
            document.getElementById('fTruong_capBac').value = (truong.capbac || '').toUpperCase();
            document.getElementById('fTruong_maDinhDanh').value = truong.madinhdanh || '';
            document.getElementById('fTruong_diaChi').value = truong.diachi || '';
            document.getElementById('fTruong_khoaList').style.display = 'none';
        } else if (level === 'KHOA') {
            var khoa = findKhoa(id);
            if (!khoa) return;
            editorPanelTitle.textContent = 'Chi tiết khoa';
            showFormSection('KHOA');
            loadTruongDropdown();
            document.getElementById('fKhoa_ten').value = khoa.ten || '';
            document.getElementById('fKhoa_maTruong').value = khoa.matruong || '';
            document.getElementById('fKhoa_nganhList').style.display = 'none';
        } else if (level === 'NGANH') {
            var nganh = findNganh(id);
            if (!nganh) return;
            editorPanelTitle.textContent = 'Chi tiết ngành';
            showFormSection('NGANH');
            loadKhoaDropdown();
            document.getElementById('fNganh_ten').value = nganh.ten || '';
            document.getElementById('fNganh_maKhoa').value = nganh.makhoa || '';
            document.getElementById('fNganh_cnList').style.display = 'none';
        } else if (level === 'CHUYEN_NGANH') {
            var cn = findChuyenNganh(id);
            if (!cn) return;
            editorPanelTitle.textContent = 'Chi tiết chuyên ngành';
            showFormSection('CHUYEN_NGANH');
            loadNganhDropdown();
            document.getElementById('fCN_ten').value = cn.ten || '';
            document.getElementById('fCN_maNganh').value = cn.manganh || '';
        }

        document.getElementById('f_level').value = level;
        setFormInputsDisabled(true);
        btnSaveEdit.style.display = 'none';
        btnCancelEdit.textContent = 'Đóng';
        showEditorPanel();
    }

    // ===================== Delete =====================
    function handleDelete(level, id) {
        var label = { TRUONG: 'trường', KHOA: 'khoa', NGANH: 'ngành', CHUYEN_NGANH: 'chuyên ngành' }[level] || 'đơn vị';
        if (!confirm('Bạn có chắc muốn xóa ' + label + ' này?')) return;

        var endpoint = { TRUONG: '/api/truong', KHOA: '/api/khoa', NGANH: '/api/nganh', CHUYEN_NGANH: '/api/chuyen-nganh' }[level];
        apiOk(endpoint + '/' + id, { method: 'DELETE' })
            .then(function () {
                showToast('Xóa thành công!');
                loadAll();
                closeEditorPanel();
            })
            .catch(function (e) {
                showToast(e.message, true);
            });
    }

    // ===================== Submit Form =====================
    formDonVi.addEventListener('submit', function (e) {
        e.preventDefault();
        var level = document.getElementById('f_level').value;
        var mode = document.getElementById('f_editMode').value;
        var editId = document.getElementById('f_editId').value;

        // Client validation
        if (!validateForm(level)) return;

        if (mode === 'view') {
            closeEditorPanel();
            return;
        }

        if (mode === 'edit') {
            submitEdit(level, editId);
            return;
        }

        // mode === 'add'
        submitAdd(level);
    });

    function validateForm(level) {
        var ok = true;

        function setError(id, hasError) {
            var group = document.getElementById(id);
            if (!group) return;
            if (hasError) {
                group.classList.add('has-error');
                ok = false;
            } else {
                group.classList.remove('has-error');
            }
        }

        if (level === 'TRUONG') {
            setError('fTruong_ten', !document.getElementById('fTruong_ten').value.trim());
            setError('fTruong_capBac', !document.getElementById('fTruong_capBac').value);
            setError('fTruong_maDinhDanh', !document.getElementById('fTruong_maDinhDanh').value.trim());
        } else if (level === 'KHOA') {
            setError('fKhoa_ten', !document.getElementById('fKhoa_ten').value.trim());
            setError('fKhoa_maTruong', !document.getElementById('fKhoa_maTruong').value);
        } else if (level === 'NGANH') {
            setError('fNganh_ten', !document.getElementById('fNganh_ten').value.trim());
            setError('fNganh_maKhoa', !document.getElementById('fNganh_maKhoa').value);
        } else if (level === 'CHUYEN_NGANH') {
            setError('fCN_ten', !document.getElementById('fCN_ten').value.trim());
            setError('fCN_maNganh', !document.getElementById('fCN_maNganh').value);
        }

        return ok;
    }

    function submitAdd(level) {
        if (level === 'TRUONG') {
            var tenKhoas = collectBatchNames('fTruong_khoaBatchList', 'khoa-name');
            var body = {
                ten: document.getElementById('fTruong_ten').value.trim(),
                capBac: document.getElementById('fTruong_capBac').value,
                maDinhDanh: document.getElementById('fTruong_maDinhDanh').value.trim(),
                diaChi: document.getElementById('fTruong_diaChi').value.trim(),
                tenKhoas: tenKhoas
            };
            apiOk('/api/truong/kem-khoa', { method: 'POST', body: JSON.stringify(body) })
                .then(function () {
                    showToast('Thêm trường thành công!');
                    loadAll();
                    closeEditorPanel();
                })
                .catch(function (e) { showToast(e.message, true); });

        } else if (level === 'KHOA') {
            var maTruong = parseInt(document.getElementById('fKhoa_maTruong').value);
            var tenKhoa = document.getElementById('fKhoa_ten').value.trim();
            var body3 = { ten: tenKhoa, maTruong: maTruong };
            apiOk('/api/khoa', { method: 'POST', body: JSON.stringify(body3) })
                .then(function () {
                    showToast('Thêm khoa thành công!');
                    loadAll();
                    closeEditorPanel();
                })
                .catch(function (e) { showToast(e.message, true); });

        } else if (level === 'NGANH') {
            var maKhoa = parseInt(document.getElementById('fNganh_maKhoa').value);
            var tenNganh = document.getElementById('fNganh_ten').value.trim();
            var tenCNs = collectBatchNames('fNganh_cnBatchList', 'cn-name');
            var tenNganhs = collectBatchNames('fNganh_nganhBatchList', 'nganh-name');
            if (tenNganhs.length === 0 && tenNganh) tenNganhs = [tenNganh];

            if (tenCNs.length > 0 && tenNganhs.length > 0) {
                var firstName = tenNganhs[0];
                apiOk('/api/nganh', { method: 'POST', body: JSON.stringify({ ten: firstName, maKhoa: maKhoa }) })
                    .then(function (res) {
                        var createdMa = res.data && res.data.ma;
                        if (!createdMa) throw new Error('Không lấy được mã ngành vừa tạo');
                        return apiOk('/api/chuyen-nganh/theo-nganh', {
                            method: 'POST',
                            body: JSON.stringify({ maNganh: createdMa, tenChuyenNganhs: tenCNs })
                        });
                    })
                    .then(function () {
                        showToast('Thêm ngành và chuyên ngành thành công!');
                        loadAll();
                        closeEditorPanel();
                    })
                    .catch(function (e) { showToast(e.message, true); });
            } else if (tenNganhs.length > 0) {
                apiOk('/api/nganh/nhieu-nganh', {
                    method: 'POST',
                    body: JSON.stringify({ maKhoa: maKhoa, tenNganhs: tenNganhs })
                })
                    .then(function () {
                        showToast('Thêm ngành thành công!');
                        loadAll();
                        closeEditorPanel();
                    })
                    .catch(function (e) { showToast(e.message, true); });
            }

        } else if (level === 'CHUYEN_NGANH') {
            apiOk('/api/chuyen-nganh', {
                method: 'POST',
                body: JSON.stringify({
                    ten: document.getElementById('fCN_ten').value.trim(),
                    maNganh: parseInt(document.getElementById('fCN_maNganh').value)
                })
            })
                .then(function () {
                    showToast('Thêm chuyên ngành thành công!');
                    loadAll();
                    closeEditorPanel();
                })
                .catch(function (e) { showToast(e.message, true); });
        }
    }

    function submitEdit(level, id) {
        if (level === 'TRUONG') {
            apiOk('/api/truong/' + id, {
                method: 'PUT',
                body: JSON.stringify({
                    ten: document.getElementById('fTruong_ten').value.trim(),
                    capBac: document.getElementById('fTruong_capBac').value,
                    maDinhDanh: document.getElementById('fTruong_maDinhDanh').value.trim(),
                    diaChi: document.getElementById('fTruong_diaChi').value.trim()
                })
            }).then(function () {
                showToast('Cập nhật thành công!');
                loadAll();
                closeEditorPanel();
            }).catch(function (e) { showToast(e.message, true); });

        } else if (level === 'KHOA') {
            apiOk('/api/khoa/' + id, {
                method: 'PUT',
                body: JSON.stringify({
                    ten: document.getElementById('fKhoa_ten').value.trim(),
                    maTruong: parseInt(document.getElementById('fKhoa_maTruong').value)
                })
            }).then(function () {
                showToast('Cập nhật thành công!');
                loadAll();
                closeEditorPanel();
            }).catch(function (e) { showToast(e.message, true); });

        } else if (level === 'NGANH') {
            apiOk('/api/nganh/' + id, {
                method: 'PUT',
                body: JSON.stringify({
                    ten: document.getElementById('fNganh_ten').value.trim(),
                    maKhoa: parseInt(document.getElementById('fNganh_maKhoa').value)
                })
            }).then(function () {
                showToast('Cập nhật thành công!');
                loadAll();
                closeEditorPanel();
            }).catch(function (e) { showToast(e.message, true); });

        } else if (level === 'CHUYEN_NGANH') {
            apiOk('/api/chuyen-nganh/' + id, {
                method: 'PUT',
                body: JSON.stringify({
                    ten: document.getElementById('fCN_ten').value.trim(),
                    maNganh: parseInt(document.getElementById('fCN_maNganh').value)
                })
            }).then(function () {
                showToast('Cập nhật thành công!');
                loadAll();
                closeEditorPanel();
            }).catch(function (e) { showToast(e.message, true); });
        }
    }

    function collectBatchNames(listId, inputClass) {
        var list = document.getElementById(listId);
        if (!list) return [];
        var result = [];
        list.querySelectorAll('.' + inputClass).forEach(function (input) {
            var val = input.value.trim();
            if (val) result.push(val);
        });
        return result;
    }

    // ===================== Dropdowns =====================
    function loadTruongDropdown() {
        var sel = document.getElementById('fKhoa_maTruong');
        var current = sel.value;
        sel.innerHTML = '<option value="">-- Chọn trường --</option>';
        allTruongs.forEach(function (t) {
            sel.insertAdjacentHTML('beforeend',
                '<option value="' + t.ma + '">' + escHtml(t.ten) + '</option>');
        });
        sel.value = current;
    }

    function loadKhoaDropdown() {
        var sel = document.getElementById('fNganh_maKhoa');
        var current = sel.value;
        sel.innerHTML = '<option value="">-- Chọn khoa --</option>';
        allKhoas.forEach(function (k) {
            var truong = findTruong(k.matruong);
            sel.insertAdjacentHTML('beforeend',
                '<option value="' + k.ma + '">' + escHtml(k.ten) + (truong ? ' (' + escHtml(truong.ten) + ')' : '') + '</option>');
        });
        sel.value = current;
    }

    function loadNganhDropdown() {
        var sel = document.getElementById('fCN_maNganh');
        var current = sel.value;
        sel.innerHTML = '<option value="">-- Chọn ngành --</option>';
        allNganhs.forEach(function (n) {
            var khoa = findKhoa(n.makhoa);
            sel.insertAdjacentHTML('beforeend',
                '<option value="' + n.ma + '">' + escHtml(n.ten) + (khoa ? ' (' + escHtml(khoa.ten) + ')' : '') + '</option>');
        });
        sel.value = current;
    }

    // ===================== Tree Action Binding =====================
    function bindTreeActions() {
        treeView.querySelectorAll('[data-action]').forEach(function (el) {
            el.addEventListener('click', function (e) {
                e.stopPropagation();
                var action = el.dataset.action;
                var type = el.dataset.type || '';
                var id = el.dataset.id || '';
                var parent = el.dataset.parent || '';
                var key = el.dataset.key || '';
                handleAction(action, type, key || id, parent);
            });
        });
    }

    // ===================== Global event listeners =====================
    btnThemTruong.addEventListener('click', function () {
        openAddForm('TRUONG', null);
    });

    btnCloseEditor.addEventListener('click', closeEditorPanel);
    btnCancelEdit.addEventListener('click', closeEditorPanel);

    btnExpandAll.addEventListener('click', function () {
        expandedNodes.clear();
        if (!defaultExpanded) {
            allTruongs.forEach(function (t) { expandedNodes.add('t_' + t.ma); });
            allKhoas.forEach(function (k) { expandedNodes.add('k_' + k.ma); });
            allNganhs.forEach(function (n) { expandedNodes.add('n_' + n.ma); });
        }
        defaultExpanded = true;
        showToast('Đã mở rộng tất cả');
        renderTree();
    });

    btnCollapseAll.addEventListener('click', function () {
        expandedNodes.clear();
        defaultExpanded = false;
        showToast('Đã thu gọn tất cả');
        renderTree();
    });

    tabs.forEach(function (tab) {
        tab.addEventListener('click', function () {
            tabs.forEach(function (t) { t.classList.remove('active'); });
            tab.classList.add('active');
            currentTab = tab.dataset.tab;
            saveTreeState();
            loadAll();
        });
    });

    function onSearchInput() {
        scheduleSearchSuggestions();
        renderTree();
    }
    searchInput.addEventListener('input', onSearchInput);
    searchInput.addEventListener('focus', function () {
        if ((searchInput.value || '').trim().length >= 1) scheduleSearchSuggestions();
    });
    if (searchSuggestions) {
        searchInput.addEventListener('blur', function () {
            setTimeout(hideSearchSuggestions, 220);
        });
        document.addEventListener('click', function (e) {
            if (searchSuggestions.style.display === 'none') return;
            var wrap = searchInput.closest('.search-input-wrapper');
            if (wrap && !wrap.contains(e.target)) hideSearchSuggestions();
        });
    }

    window.addEventListener('beforeunload', function () {
        clearTimeout(saveTreeStateTimer);
        saveTreeState();
    });

    // ===================== Init =====================
    closeEditorPanel();
    loadAll();

})();
