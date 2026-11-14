-- =============================================================================
-- BookShop - Dua diem trung binh danh gia ve thang bang books
--
-- Truoc: loc "sach co diem >= N" phai chay subquery tuong quan tren bang ratings
--        cho TUNG dong ung vien truoc khi LIMIT => 5.000 sach la 5.000 lan probe.
-- Sau:   loc thang tren cot average_stars co index => mot lan index scan.
--
-- Cot do Hibernate tu them (ddl-auto=update). File nay lo phan ddl-auto KHONG lam duoc:
-- dien du lieu cho cac ban ghi da co san.
-- =============================================================================

ALTER TABLE public.books ADD COLUMN IF NOT EXISTS average_stars double precision;
ALTER TABLE public.books ADD COLUMN IF NOT EXISTS rating_count integer;

UPDATE public.books b
SET average_stars = COALESCE(r.avg_stars, 0),
    rating_count  = COALESCE(r.total, 0)
FROM (
    SELECT book_id, AVG(stars) AS avg_stars, COUNT(*) AS total
    FROM public.ratings
    GROUP BY book_id
) r
WHERE b.id = r.book_id;

UPDATE public.books SET average_stars = 0 WHERE average_stars IS NULL;
UPDATE public.books SET rating_count  = 0 WHERE rating_count  IS NULL;

ALTER TABLE public.books ALTER COLUMN average_stars SET NOT NULL;
ALTER TABLE public.books ALTER COLUMN rating_count  SET NOT NULL;
ALTER TABLE public.books ALTER COLUMN average_stars SET DEFAULT 0;
ALTER TABLE public.books ALTER COLUMN rating_count  SET DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_books_average_stars ON public.books (average_stars);

ANALYZE public.books;
