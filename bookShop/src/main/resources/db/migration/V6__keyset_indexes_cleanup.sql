-- =============================================================================
-- BookShop - Chinh lai bo index cho khop keyset pagination
--
-- Keyset so sanh bo doi (gia_tri, id) nen index phai co ca hai cot. Sau khi da co
-- (price, id) va (average_stars, id) thi index mot cot (price) va (average_stars)
-- tro thanh THUA: chung la tien to cua index gop, planner dung index gop duoc ca cho
-- truy van chi loc theo mot cot. Giu lai chi ton dia va lam cham moi lan INSERT/UPDATE.
--
-- ddl-auto=update chi THEM index, khong bao gio XOA - nen phai xoa tay o day.
-- =============================================================================

DROP INDEX IF EXISTS public.idx_books_price;
DROP INDEX IF EXISTS public.idx_books_average_stars;
DROP INDEX IF EXISTS public.idx_books_category_price;

-- Loc the loai roi sap xep - hai to hop hay gap nhat o man hinh khach
CREATE INDEX IF NOT EXISTS idx_books_category_created_at_id
    ON public.books (category_id, created_at, id);

CREATE INDEX IF NOT EXISTS idx_books_category_price_id
    ON public.books (category_id, price, id);

ANALYZE public.books;
