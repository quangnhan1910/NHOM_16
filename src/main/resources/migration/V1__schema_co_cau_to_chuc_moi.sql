-- ================================================================
-- MIGRATION: Schema mới cho Cơ cấu Tổ chức
-- Chạy THỦ CÔNG trong phpMyAdmin (XAMPP) → tab SQL, dán và Execute
-- Database: thitracnghiem
-- ================================================================
-- Sau khi chạy xong: khởi động lại ứng dụng Spring Boot.
-- ================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Rename bảng truong_dai_hoc → truong
RENAME TABLE `thitracnghiem`.`truong_dai_hoc` TO `thitracnghiem`.`truong`;

-- 2. Thêm cột cap_bac vào bảng truong (mặc định DAI_HOC cho dữ liệu cũ)
ALTER TABLE `thitracnghiem`.`truong`
    ADD COLUMN `cap_bac` VARCHAR(20) DEFAULT 'DAI_HOC' AFTER `dia_chi`;

-- 3. Bảng khoa: drop FK cũ (tự lấy tên constraint từ information_schema)
SET @fk_khoa = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'thitracnghiem'
      AND TABLE_NAME = 'khoa'
      AND COLUMN_NAME = 'ma_truong_dai_hoc'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql_khoa = IF(@fk_khoa IS NOT NULL,
    CONCAT('ALTER TABLE `thitracnghiem`.`khoa` DROP FOREIGN KEY `', @fk_khoa, '`'),
    'SELECT 1');
PREPARE stmt_khoa FROM @sql_khoa;
EXECUTE stmt_khoa;
DEALLOCATE PREPARE stmt_khoa;

-- 4. Đổi tên cột FK trong khoa: ma_truong_dai_hoc → ma_truong
ALTER TABLE `thitracnghiem`.`khoa`
    CHANGE COLUMN `ma_truong_dai_hoc` `ma_truong` INT NOT NULL;

-- 5. Bảng khoa: bỏ cột ma_dinh_danh (chỉ chạy nếu cột tồn tại)
SET @has_mdd = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = 'thitracnghiem' AND TABLE_NAME = 'khoa' AND COLUMN_NAME = 'ma_dinh_danh');
SET @sql_mdd = IF(@has_mdd > 0, 'ALTER TABLE `thitracnghiem`.`khoa` DROP COLUMN `ma_dinh_danh`', 'SELECT 1');
PREPARE stmt_mdd FROM @sql_mdd;
EXECUTE stmt_mdd;
DEALLOCATE PREPARE stmt_mdd;

-- 6. Thêm lại FK khoa → truong
ALTER TABLE `thitracnghiem`.`khoa`
    ADD CONSTRAINT `fk_khoa_truong` FOREIGN KEY (`ma_truong`)
    REFERENCES `thitracnghiem`.`truong` (`ma`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 7. Bảng quan_tri_vien_he_thong: drop FK cũ (tự lấy tên constraint)
SET @fk_qtv = (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = 'thitracnghiem'
      AND TABLE_NAME = 'quan_tri_vien_he_thong'
      AND COLUMN_NAME = 'ma_truong_dai_hoc'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);
SET @sql_qtv = IF(@fk_qtv IS NOT NULL,
    CONCAT('ALTER TABLE `thitracnghiem`.`quan_tri_vien_he_thong` DROP FOREIGN KEY `', @fk_qtv, '`'),
    'SELECT 1');
PREPARE stmt_qtv FROM @sql_qtv;
EXECUTE stmt_qtv;
DEALLOCATE PREPARE stmt_qtv;

-- 8. Đổi tên cột FK: ma_truong_dai_hoc → ma_truong
ALTER TABLE `thitracnghiem`.`quan_tri_vien_he_thong`
    CHANGE COLUMN `ma_truong_dai_hoc` `ma_truong` INT NOT NULL;

-- 9. Thêm lại FK quan_tri_vien_he_thong → truong
ALTER TABLE `thitracnghiem`.`quan_tri_vien_he_thong`
    ADD CONSTRAINT `fk_qtv_truong` FOREIGN KEY (`ma_truong`)
    REFERENCES `thitracnghiem`.`truong` (`ma`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 10. Bảng nganh: bỏ cột bac_dao_tao và ma_dinh_danh
ALTER TABLE `thitracnghiem`.`nganh`
    DROP COLUMN `bac_dao_tao`,
    DROP COLUMN `ma_dinh_danh`;

-- 11. Bảng chuyen_nganh: bỏ cột ma_dinh_danh
ALTER TABLE `thitracnghiem`.`chuyen_nganh`
    DROP COLUMN `ma_dinh_danh`;

SET FOREIGN_KEY_CHECKS = 1;
