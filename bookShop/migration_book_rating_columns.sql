-- Vá schema cho DB đang có dữ liệu: thêm 2 cột average_stars / rating_count vào books.
-- ddl-auto=update sinh "ADD COLUMN ... NOT NULL" không kèm DEFAULT nên Postgres từ chối
-- khi bảng đã có bản ghi. Thêm DEFAULT để Postgres tự backfill, rồi tính lại theo ratings.

ALTER TABLE books ADD COLUMN IF NOT EXISTS average_stars double precision NOT NULL DEFAULT 0;
ALTER TABLE books ADD COLUMN IF NOT EXISTS rating_count integer NOT NULL DEFAULT 0;

-- Tính lại điểm trung bình + số lượt đánh giá từ dữ liệu ratings hiện có.
UPDATE books b
SET average_stars = COALESCE(r.avg_stars, 0),
    rating_count  = COALESCE(r.cnt, 0)
FROM (
    SELECT book_id, AVG(stars) AS avg_stars, COUNT(*) AS cnt
    FROM ratings
    GROUP BY book_id
) r
WHERE b.id = r.book_id;

CREATE INDEX IF NOT EXISTS idx_books_average_stars ON books (average_stars);
