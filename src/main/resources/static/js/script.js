document.addEventListener('DOMContentLoaded', () => {
    // Tự động Active menu dựa trên tên file
    const currentPath = window.location.pathname.split('/').pop() || 'index.html';
    
    document.querySelectorAll('.nav-item').forEach(item => {
        const href = item.getAttribute('href');
        if (href === currentPath) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    // Xử lý nút Đăng xuất
    document.getElementById('btn-logout')?.addEventListener('click', () => {
        if (confirm('Bạn có chắc chắn muốn đăng xuất không?')) {
            alert('Đã đăng xuất!');
        }
    });
});