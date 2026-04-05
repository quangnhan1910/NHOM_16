document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const togglePasswordBtn = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('password');
    const rememberCheckbox = document.getElementById('rememberMe');

    // Toggle password visibility
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', function() {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            // Update icon based on visibility
            const icon = togglePasswordBtn.querySelector('svg');
            if (type === 'text') {
                icon.innerHTML = `
                    <path d="M11 3C7 3 3.5 6 2 9C3.5 12 7 15 11 15C15 15 18.5 12 20 9M11 6V9M11 9L8 11M11 9L14 11" stroke="#94A3B8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                `;
            } else {
                icon.innerHTML = `
                    <path d="M11 3C7 3 3.5 6 2 9C3.5 12 7 15 11 15C15 15 18.5 12 20 9C18.5 6 15 3 11 3Z" stroke="#94A3B8" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                    <circle cx="11" cy="9" r="2" stroke="#94A3B8" stroke-width="1.5"/>
                `;
            }
        });
    }

    // Handle form submission
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            // Form will be submitted normally by Spring Security
            // But we can add custom handling here if needed
        });
    }

    // Clear error message on input
    const emailInput = document.getElementById('email');
    if (emailInput) {
        emailInput.addEventListener('input', function() {
            const errorMsg = document.querySelector('.error-message');
            if (errorMsg) {
                errorMsg.style.display = 'none';
            }
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener('input', function() {
            const errorMsg = document.querySelector('.error-message');
            if (errorMsg) {
                errorMsg.style.display = 'none';
            }
        });
    }
});
