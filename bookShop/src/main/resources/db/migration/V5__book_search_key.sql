-- =============================================================================
-- BookShop - Cot tim kiem chuan hoa cho endpoint /api/v1/books/search
--
-- search_key = lower(bo_dau(name + ' ' + author)), tinh trong Java (@PrePersist/@PreUpdate).
--
-- Vi sao khong dung tsvector cua Postgres: tang Java tinh san thi cung mot cau truy van
-- chay dung nhu nhau tren moi he quan tri, nen viet duoc test that. Tren Postgres van
-- duoc index trigram duoi day tang toc y het.
--
-- Cot do Hibernate tu them (ddl-auto=update). File nay lo phan ddl-auto khong lam duoc:
-- dien du lieu cho cac ban ghi da co san.
-- =============================================================================

ALTER TABLE public.books ADD COLUMN IF NOT EXISTS search_key varchar(250);

-- unaccent khong phai extension mac dinh nen dung translate: liet ke nguyen am tieng Viet co dau.
-- Chi chay MOT lan de dien du lieu cu; tu day ve sau tang Java lo viec cap nhat.
UPDATE public.books
SET search_key = lower(translate(
        name || ' ' || author,
        'ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵýỷỹ',
        'AAAAEEEIIOOOOUUADIUOaaaaeeeiioooouuadiuoUAAAAAAAAAAAAAEEEEEEuaaaaaaaaaaaaaeeeeeeEEIIOOOOOOOOOOOOUUUUeeiioooooooooooouuuuUUUYYYYYuuuyyyyy'))
WHERE search_key IS NULL;

-- Tim kiem la LIKE '%tu-khoa%' nen B-tree vo dung, phai la trigram
CREATE INDEX IF NOT EXISTS idx_books_search_key_trgm
    ON public.books USING gin (search_key gin_trgm_ops);

-- Keyset phan trang: so sanh bo doi (gia_tri, id) nen index phai co ca hai cot
CREATE INDEX IF NOT EXISTS idx_books_price_id         ON public.books (price, id);
CREATE INDEX IF NOT EXISTS idx_books_average_stars_id ON public.books (average_stars DESC, id DESC);

ANALYZE public.books;
