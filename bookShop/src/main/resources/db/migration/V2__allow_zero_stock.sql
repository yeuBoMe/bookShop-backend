-- =============================================================================
-- BookShop - Cho phep ton kho ve 0
-- CHECK cu (quantity >= 1) khien sach KHONG BAO GIO ban het duoc:
-- khi tru ton kho cuon cuoi cung, DB tu choi UPDATE.
-- =============================================================================

ALTER TABLE public.books DROP CONSTRAINT IF EXISTS books_quantity_check;

ALTER TABLE public.books
    ADD CONSTRAINT books_quantity_check CHECK (quantity >= 0 AND quantity <= 1000);
