-- =============================================================================
-- BookShop - Index PostgreSQL cho tim kiem (phan @Index tren entity KHONG khai duoc)
--
-- Index B-tree thong thuong (khoa ngoai, created_at, price, status...) da duoc khai
-- bang @Table(indexes = @Index(...)) tren entity nen Hibernate tu tao khi khoi dong.
-- File nay chi chua thu JPA khong dien ta duoc:
--   - GIN + pg_trgm cho LIKE '%tu-khoa%'  (springfilter toan tu ~~)
--   - cot tsvector sinh tu dong cho full-text search
--
-- Chay 1 lan tren moi moi truong: psql -U <user> -d <db> -f V1__search_indexes.sql
-- Idempotent: chay lai nhieu lan khong loi.
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- -----------------------------------------------------------------------------
-- 1. Trigram cho o tim kiem. B-tree vo dung voi LIKE '%x%' vi khong co tien to co dinh.
-- -----------------------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_books_name_trgm      ON public.books      USING gin (lower(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_books_author_trgm    ON public.books      USING gin (lower(author) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_categories_name_trgm ON public.categories USING gin (lower(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_fullname_trgm  ON public.users      USING gin (lower(full_name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_users_email_trgm     ON public.users      USING gin (lower(email) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_orders_name_trgm     ON public.orders     USING gin (lower(name) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_orders_address_trgm  ON public.orders     USING gin (lower(address) gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_ratings_comment_trgm ON public.ratings    USING gin (lower(comment) gin_trgm_ops);

-- -----------------------------------------------------------------------------
-- 2. Full-text search cho sach.
--    Cot sinh tu dong => khong can trigger, khong can sua tang Java khi insert/update.
--    Dung config 'simple': tieng Viet khong co bo stem trong Postgres, 'simple' chi
--    tach token + lowercase nen giu nguyen tu co dau, khong cat sai goc tu.
-- -----------------------------------------------------------------------------
ALTER TABLE public.books
    ADD COLUMN IF NOT EXISTS search_vector tsvector
    GENERATED ALWAYS AS (
        setweight(to_tsvector('simple', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('simple', coalesce(author, '')), 'B') ||
        setweight(to_tsvector('simple', coalesce(introduce_text, '')), 'C') ||
        setweight(to_tsvector('simple', coalesce(description, '')), 'D')
    ) STORED;

CREATE INDEX IF NOT EXISTS idx_books_search_vector ON public.books USING gin (search_vector);

-- -----------------------------------------------------------------------------
-- 3. Functional index cho cac query so sanh LOWER(...)
--    @Index tren entity chi nhan TEN COT tran, khong nhan bieu thuc,
--    nen nhung index nay bat buoc phai khai o day.
--    Khong co chung thi LOWER(name) = ? van quet toan bang du cot name da co index.
-- -----------------------------------------------------------------------------

-- BookRepository.existsByNameAndAuthorIgnoreCase - chay moi lan tao/sua sach
CREATE INDEX IF NOT EXISTS idx_books_lower_name_author
    ON public.books (lower(name), lower(author));

-- CategoryRepository.existsByNameIgnoreCase / findExistingNamesIgnoreCase / findAllByNamesIgnoreCase
CREATE INDEX IF NOT EXISTS idx_categories_lower_name
    ON public.categories (lower(name));

-- RoleRepository.existsByNameIgnoreCase / findExistingNamesIgnoreCase
CREATE INDEX IF NOT EXISTS idx_roles_lower_name
    ON public.roles (lower(name));

-- PermissionRepository.existsByNameIgnoreCase
CREATE INDEX IF NOT EXISTS idx_permissions_lower_name
    ON public.permissions (lower(name));

-- -----------------------------------------------------------------------------
-- 4. Cap nhat thong ke cho planner
-- -----------------------------------------------------------------------------
ANALYZE public.books;
ANALYZE public.orders;
ANALYZE public.order_details;
ANALYZE public.ratings;
ANALYZE public.users;
ANALYZE public.categories;
