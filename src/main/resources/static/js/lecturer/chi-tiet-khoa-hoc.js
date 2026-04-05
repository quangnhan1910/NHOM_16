/**
 * Chi tiết khóa học - Lecturer Course Detail
 * Xử lý tab navigation và các tương tác
 */

document.addEventListener('DOMContentLoaded', function() {
    // Tab navigation
    const tabs = document.querySelectorAll('.tab');
    tabs.forEach((tab, index) => {
        tab.addEventListener('click', function() {
            // Remove active class from all tabs
            tabs.forEach(t => t.classList.remove('active'));
            // Add active class to clicked tab
            this.classList.add('active');
            
            // Handle tab content switching (if needed)
            console.log('Tab switched to:', this.textContent.trim());
        });
    });

    // Search functionality
    const searchInput = document.querySelector('.search-box input');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            const tableRows = document.querySelectorAll('.schedule-table tbody tr');
            
            tableRows.forEach(row => {
                const cells = row.querySelectorAll('td');
                let match = false;
                
                cells.forEach(cell => {
                    if (cell.textContent.toLowerCase().includes(searchTerm)) {
                        match = true;
                    }
                });
                
                row.style.display = match ? '' : 'none';
            });
        });
    }

    // Format dates on load
    const dateCells = document.querySelectorAll('.schedule-table td:nth-child(2)');
    dateCells.forEach(cell => {
        const text = cell.textContent.trim();
        if (text && text.includes('-')) {
            // Already formatted from Thymeleaf
        }
    });

    // Status badge tooltips
    const badges = document.querySelectorAll('.status-badge');
    badges.forEach(badge => {
        badge.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.05)';
        });
        badge.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    });

    // Table row hover effect
    const tableRows = document.querySelectorAll('.schedule-table tbody tr');
    tableRows.forEach(row => {
        row.addEventListener('click', function() {
            // Optional: Add click to view detail
            console.log('Row clicked');
        });
    });
});
