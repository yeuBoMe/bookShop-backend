-- ============================================================
-- Fake data cho BookShop
-- Chay tren schema DA CO SAN (ddl-auto=update da tao bang roi)
-- Mat khau cua MOI tai khoan: 123456
--   (BCrypt strength 10, sinh bang chinh spring-security-crypto 6.5.3 cua project)
--   LUU Y: LoginRequest co @Size(min = 6) nen mat khau NGAN HON 6 ky tu se bi
--   tra 400 truoc khi toi buoc so khop BCrypt.
--
-- Chay:
--   psql -h localhost -p 5433 -U postgres -d book_shop -f fake_data.sql
-- ============================================================

BEGIN;

-- ------------------------------------------------------------
-- 1. ROLES  --  DatabaseInitializer BAT BUOC tim thay role ten 'ADMIN'
--               (thieu -> NoSuchElementException -> app khong start duoc)
-- ------------------------------------------------------------
INSERT INTO public.roles (id, active, created_at, created_by, description, name) VALUES
  (1, true, now(), 'system', 'Quan tri vien - toan quyen he thong', 'ADMIN'),
  (2, true, now(), 'system', 'Nguoi dung thong thuong',             'USER')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- 2. USERS  --  DatabaseInitializer BAT BUOC tim thay email 'ninh@gmail.com'
-- ------------------------------------------------------------
INSERT INTO public.users (id, full_name, email, password, phone, avatar, created_at, created_by, role_id) VALUES
  (1, 'Ly Hoai Ninh',  'ninh@gmail.com',  '$2a$10$Miy9n.BdM5vnpYAJlXMseOKxuF6/DX919cd0ytasHZmrlaGe/nEhu', '0912345671', '1759912924914_ly-hoai-ninh.png', now(), 'system', 1),
  (2, 'Nguyen Van An',  'an@gmail.com',    '$2a$10$Miy9n.BdM5vnpYAJlXMseOKxuF6/DX919cd0ytasHZmrlaGe/nEhu', '0912345672', '1762184431850_avt_mac_dinh.jpg', now(), 'system', 2),
  (3, 'Tran Thi Binh',  'binh@gmail.com',  '$2a$10$Miy9n.BdM5vnpYAJlXMseOKxuF6/DX919cd0ytasHZmrlaGe/nEhu', '0912345673', '1762184431850_avt_mac_dinh.jpg', now(), 'system', 2),
  (4, 'Le Van Chien',   'chien@gmail.com', '$2a$10$Miy9n.BdM5vnpYAJlXMseOKxuF6/DX919cd0ytasHZmrlaGe/nEhu', '0912345674', '1762184431850_avt_mac_dinh.jpg', now(), 'system', 2),
  (5, 'Pham Thi Dung',  'dung@gmail.com',  '$2a$10$Miy9n.BdM5vnpYAJlXMseOKxuF6/DX919cd0ytasHZmrlaGe/nEhu', '0912345675', '1760958229280_test.png',         now(), 'system', 2)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- 3. CATEGORIES
-- ------------------------------------------------------------
INSERT INTO public.categories (id, name, created_at, created_by) VALUES
  (1, 'Manga',                now(), 'ninh@gmail.com'),
  (2, 'Cong nghe thong tin',  now(), 'ninh@gmail.com'),
  (3, 'Van hoc',              now(), 'ninh@gmail.com'),
  (4, 'Khoa hoc vien tuong',  now(), 'ninh@gmail.com'),
  (5, 'Kinh te',              now(), 'ninh@gmail.com'),
  (6, 'Tam ly - Ky nang',     now(), 'ninh@gmail.com'),
  (7, 'Thieu nhi',            now(), 'ninh@gmail.com'),
  (8, 'Lich su',              now(), 'ninh@gmail.com')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- 4. BOOKS  --  quantity phai trong [1, 1000] (books_quantity_check)
--               images la mang varchar(255)[] ; ten file lay tu upload/book/
-- ------------------------------------------------------------
INSERT INTO public.books (id, name, author, price, quantity, sold, category_id, images, description, introduce_text, reader, created_at, created_by) VALUES
  (1,  'Clean Code',                      'Robert C. Martin',  350000, 120,  '450', 2, ARRAY['1758618433836_clean_code.jpg','1758618433833_clean_code_v2.jpg','1758618433831_clean_code_v3.jpg']::varchar(255)[], 'Cam nang viet code sach, de doc, de bao tri cho lap trinh vien moi cap do.',    'Cuon sach gia dinh cua moi developer chuyen nghiep.',      'Lap trinh vien',      now(), 'ninh@gmail.com'),
  (2,  'Clean Architecture',              'Robert C. Martin',  320000,  85,  '210', 2, ARRAY['1758618433836_clean_code.jpg']::varchar(255)[],                                                                              'Nguyen tac thiet ke kien truc phan mem ben vung theo thoi gian.',              'Hieu ro ranh gioi giua business logic va framework.',      'Kien truc su phan mem', now(), 'ninh@gmail.com'),
  (3,  'Detective Conan Tap 1',           'Gosho Aoyama',       25000, 300,  '980', 1, ARRAY['1758619128279_conan_the_time_bombed_skyscraper.jpg']::varchar(255)[],                                                        'Tap dau tien cua bo truyen trinh tham noi tieng nhat Nhat Ban.',               'Khoi dau hanh trinh cua tham tu Kudo Shinichi.',           'Thieu nien',          now(), 'ninh@gmail.com'),
  (4,  'Detective Conan Tap 2',           'Gosho Aoyama',       25000, 280,  '870', 1, ARRAY['1758619128279_conan_the_time_bombed_skyscraper.jpg']::varchar(255)[],                                                        'Nhung vu an moi tiep tuc thu thach tri tue cua Conan.',                        'Manh moi ve To chuc Ao den dan he lo.',                    'Thieu nien',          now(), 'ninh@gmail.com'),
  (5,  'Dragon Ball Super Broly',         'Akira Toriyama',     45000, 150,  '640', 1, ARRAY['1758618689697_dbs_broly.jpg']::varchar(255)[],                                                                               'Cuoc chien giua Goku, Vegeta va chien binh Saiyan huyen thoai Broly.',         'Phien ban truyen tranh cua bo phim dien anh an khach.',    'Thieu nien',          now(), 'ninh@gmail.com'),
  (6,  'Nha Gia Kim',                     'Paulo Coelho',       89000, 200, '1520', 3, ARRAY['1760958229280_test.png']::varchar(255)[],                                                                                    'Hanh trinh di tim kho bau va y nghia cuoc doi cua chang trai chan cuu Santiago.', 'Tieu thuyet duoc dich ra hon 80 ngon ngu.',              'Moi lua tuoi',        now(), 'ninh@gmail.com'),
  (7,  'Dac Nhan Tam',                    'Dale Carnegie',      98000, 175, '2100', 6, ARRAY['1760958229280_test.png']::varchar(255)[],                                                                                    'Nghe thuat doi nhan xu the va xay dung moi quan he ben vung.',                 'Cuon sach ky nang song ban chay nhat moi thoi dai.',       'Nguoi di lam',        now(), 'ninh@gmail.com'),
  (8,  'Tu Duy Nhanh Va Cham',            'Daniel Kahneman',   189000,  90,  '430', 6, ARRAY['1760958229280_test.png']::varchar(255)[],                                                                                    'Hai he thong tu duy chi phoi moi quyet dinh cua con nguoi.',                   'Cong trinh doat giai Nobel Kinh te.',                      'Nguoi di lam',        now(), 'ninh@gmail.com'),
  (9,  'Sapiens - Luoc Su Loai Nguoi',    'Yuval Noah Harari', 215000, 110,  '760', 8, ARRAY['1760958229280_test.png']::varchar(255)[],                                                                                    'Hanh trinh 70.000 nam tu loai vuon den ke thong tri hanh tinh.',               'Goc nhin moi me ve lich su nhan loai.',                    'Moi lua tuoi',        now(), 'ninh@gmail.com'),
  (10, 'Dune',                            'Frank Herbert',     250000,  70,  '310', 4, ARRAY['1760958229280_test.png']::varchar(255)[],                                                                                    'Su thi khoa hoc vien tuong tren hanh tinh sa mac Arrakis.',                    'Nen tang cua dong tieu thuyet vien tuong hien dai.',       'Nguoi lon',           now(), 'ninh@gmail.com'),
  (11, 'Nguoi Giau Co Nhat Thanh Babylon','George S. Clason',   75000, 230, '1180', 5, ARRAY['1760958229280_test.png']::varchar(255)[],                                                                                    'Bai hoc quan ly tai chinh ca nhan qua nhung du ngon co dai.',                  'Nguyen tac lam giau don gian ma ben vung.',                'Nguoi di lam',        now(), 'ninh@gmail.com'),
  (12, 'De Men Phieu Luu Ky',             'To Hoai',            55000, 260, '1340', 7, ARRAY['1760958229280_test.png']::varchar(255)[],                                                                                    'Cuoc phieu luu truong thanh cua chu De Men qua the gioi con trung.',           'Tac pham kinh dien cua van hoc thieu nhi Viet Nam.',       'Thieu nhi',           now(), 'ninh@gmail.com')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- 5. ORDERS  --  status chi duoc: SUCCESS | PENDING | CANCEL | SHIPPING | PAID | UNPAID
-- ------------------------------------------------------------
INSERT INTO public.orders (id, name, email, phone, address, payment_method, status, total_price, created_at, created_by) VALUES
  (1, 'Nguyen Van An', 'an@gmail.com',    '0912345672', '12 Nguyen Trai, Thanh Xuan, Ha Noi',  'COD',   'SUCCESS',  439000, now(), 'an@gmail.com'),
  (2, 'Tran Thi Binh', 'binh@gmail.com',  '0912345673', '45 Le Loi, Quan 1, TP Ho Chi Minh',   'VNPAY', 'SHIPPING', 185000, now(), 'binh@gmail.com'),
  (3, 'Le Van Chien',  'chien@gmail.com', '0912345674', '88 Tran Phu, Hai Chau, Da Nang',      'MOMO',  'PENDING',  320000, now(), 'chien@gmail.com'),
  (4, 'Pham Thi Dung', 'dung@gmail.com',  '0912345675', '7 Hoang Van Thu, Ninh Kieu, Can Tho', 'COD',   'CANCEL',    98000, now(), 'dung@gmail.com')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- 6. ORDER_DETAILS
-- ------------------------------------------------------------
INSERT INTO public.order_details (id, order_id, book_id, book_name, quantity) VALUES
  (1, 1, 1, 'Clean Code',              1),
  (2, 1, 6, 'Nha Gia Kim',             1),
  (3, 2, 3, 'Detective Conan Tap 1',   2),
  (4, 2, 5, 'Dragon Ball Super Broly', 3),
  (5, 3, 2, 'Clean Architecture',      1),
  (6, 4, 7, 'Dac Nhan Tam',            1)
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- 7. RATINGS  --  stars 1..5 (ratings_stars_check), UNIQUE(user_id, book_id)
-- ------------------------------------------------------------
INSERT INTO public.ratings (id, user_id, book_id, stars, comment, created_at, created_by) VALUES
  (1, 2, 1,  5, 'Sach hay, moi developer nen doc it nhat mot lan.', now(), 'an@gmail.com'),
  (2, 3, 1,  4, 'Noi dung tot nhung vi du hoi cu.',                 now(), 'binh@gmail.com'),
  (3, 2, 6,  5, 'Truyen cuon hut, doc mot mach het luon.',          now(), 'an@gmail.com'),
  (4, 4, 3,  4, 'Con minh rat thich bo truyen nay.',                now(), 'chien@gmail.com'),
  (5, 5, 7,  5, 'Kinh dien, doc lai van thay hay.',                 now(), 'dung@gmail.com'),
  (6, 3, 10, 3, 'Hoi kho doc voi nguoi moi bat dau.',               now(), 'binh@gmail.com')
ON CONFLICT (id) DO NOTHING;

-- ------------------------------------------------------------
-- 8. Reset sequence  --  BAT BUOC vi phia tren insert ID tuong minh.
--    Bo qua buoc nay -> tao moi qua API se loi duplicate key.
-- ------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('public.roles','id'),         (SELECT COALESCE(MAX(id),1) FROM public.roles));
SELECT setval(pg_get_serial_sequence('public.users','id'),         (SELECT COALESCE(MAX(id),1) FROM public.users));
SELECT setval(pg_get_serial_sequence('public.categories','id'),    (SELECT COALESCE(MAX(id),1) FROM public.categories));
SELECT setval(pg_get_serial_sequence('public.books','id'),         (SELECT COALESCE(MAX(id),1) FROM public.books));
SELECT setval(pg_get_serial_sequence('public.orders','id'),        (SELECT COALESCE(MAX(id),1) FROM public.orders));
SELECT setval(pg_get_serial_sequence('public.order_details','id'), (SELECT COALESCE(MAX(id),1) FROM public.order_details));
SELECT setval(pg_get_serial_sequence('public.ratings','id'),       (SELECT COALESCE(MAX(id),1) FROM public.ratings));

COMMIT;
