-- ============================================================
-- CycleMart PostgreSQL Schema
-- Generated from full_schema_v9_final.html
-- ============================================================

-- Drop tables in reverse dependency order (nếu cần reset)
DROP TABLE IF EXISTS Reviews CASCADE;
DROP TABLE IF EXISTS Review_Tags CASCADE;
DROP TABLE IF EXISTS Notifications CASCADE;
DROP TABLE IF EXISTS Payment_Logs CASCADE;
DROP TABLE IF EXISTS Payments CASCADE;
DROP TABLE IF EXISTS Escrow CASCADE;
DROP TABLE IF EXISTS Orders CASCADE;
DROP TABLE IF EXISTS Conversations CASCADE;
DROP TABLE IF EXISTS User_Tracking CASCADE;
DROP TABLE IF EXISTS Guest_Tracking CASCADE;
DROP TABLE IF EXISTS Listing_Boosts CASCADE;
DROP TABLE IF EXISTS Listing_Packages CASCADE;
DROP TABLE IF EXISTS Product_Images CASCADE;
DROP TABLE IF EXISTS Products CASCADE;
DROP TABLE IF EXISTS Shops CASCADE;
DROP TABLE IF EXISTS Categories CASCADE;
DROP TABLE IF EXISTS Sessions CASCADE;
DROP TABLE IF EXISTS Bank_Accounts CASCADE;
DROP TABLE IF EXISTS Users CASCADE;

-- ─── Users ──────────────────────────────────────────────────
CREATE TABLE Users (
    id            SERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    full_name     VARCHAR(255) NOT NULL,
    phone         VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('BUYER', 'SELLER')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'BANNED', 'SUSPENDED')),
    ban_reason    TEXT,
    banned_at     TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMPTZ
);

-- ─── Bank_Accounts ──────────────────────────────────────────
CREATE TABLE Bank_Accounts (
    id             SERIAL PRIMARY KEY,
    user_id        INT          NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    bank_name      VARCHAR(100) NOT NULL,
    account_number VARCHAR(50)  NOT NULL,
    account_name   VARCHAR(255) NOT NULL,
    is_verified    BOOLEAN      NOT NULL DEFAULT FALSE,
    verified_at    TIMESTAMPTZ,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─── Sessions ───────────────────────────────────────────────
CREATE TABLE Sessions (
    id               VARCHAR(128) PRIMARY KEY,
    user_id          INT REFERENCES Users(id) ON DELETE CASCADE,
    guest_session_id VARCHAR(128),
    token            TEXT,
    expires_at       TIMESTAMPTZ,
    revoked_at       TIMESTAMPTZ,
    converted_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── Categories ─────────────────────────────────────────────
CREATE TABLE Categories (
    id        SERIAL PRIMARY KEY,
    parent_id INT REFERENCES Categories(id) ON DELETE SET NULL,
    name      VARCHAR(100) NOT NULL
);

-- ─── Shops ──────────────────────────────────────────────────
CREATE TABLE Shops (
    id          SERIAL PRIMARY KEY,
    user_id     INT          NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    category_id INT          REFERENCES Categories(id) ON DELETE SET NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    avatar      VARCHAR(500),
    status      VARCHAR(20)  NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'banned')),
    ban_reason  TEXT,
    banned_at   TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─── Products ───────────────────────────────────────────────
CREATE TABLE Products (
    id                   SERIAL PRIMARY KEY,
    user_id              INT             NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    shop_id              INT             REFERENCES Shops(id) ON DELETE SET NULL,
    category_id          INT             REFERENCES Categories(id) ON DELETE SET NULL,
    name                 VARCHAR(255)    NOT NULL,
    price                DECIMAL(15, 2)  NOT NULL,
    condition            VARCHAR(50),
    status               VARCHAR(20)     NOT NULL DEFAULT 'active'
                             CHECK (status IN ('active', 'reserved', 'sold', 'hidden', 'violated')),
    reserved_for_user_id INT             REFERENCES Users(id) ON DELETE SET NULL,
    hide_reason          TEXT,
    hidden_at            TIMESTAMPTZ,
    created_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- ─── Product_Images ─────────────────────────────────────────
CREATE TABLE Product_Images (
    id         SERIAL PRIMARY KEY,
    product_id INT          NOT NULL REFERENCES Products(id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,  -- "order" là từ khóa reserved trong PostgreSQL
    is_primary BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ─── Listing_Packages ───────────────────────────────────────
CREATE TABLE Listing_Packages (
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(50)    NOT NULL CHECK (name IN ('free', 'featured', 'top')),
    price         DECIMAL(15, 2) NOT NULL,
    duration_days INT            NOT NULL,
    description   TEXT
);

-- ─── Listing_Boosts ─────────────────────────────────────────
CREATE TABLE Listing_Boosts (
    id             SERIAL PRIMARY KEY,
    product_id     INT         NOT NULL REFERENCES Products(id) ON DELETE CASCADE,
    user_id        INT         NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    package_id     INT         NOT NULL REFERENCES Listing_Packages(id),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (payment_status IN ('pending', 'paid', 'failed')),
    payment_ref    VARCHAR(255),
    started_at     TIMESTAMPTZ,
    expires_at     TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── Guest_Tracking ─────────────────────────────────────────
CREATE TABLE Guest_Tracking (
    id          SERIAL PRIMARY KEY,
    session_id  VARCHAR(128) REFERENCES Sessions(id) ON DELETE SET NULL,
    event_type  VARCHAR(100),
    location    VARCHAR(255),
    product_id  INT          REFERENCES Products(id) ON DELETE SET NULL,
    seller_id   INT,   -- BE lookup, không FK cứng
    category_id INT,   -- BE lookup, không FK cứng
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── User_Tracking ──────────────────────────────────────────
CREATE TABLE User_Tracking (
    id          SERIAL PRIMARY KEY,
    user_id     INT          NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    session_id  VARCHAR(128) REFERENCES Sessions(id) ON DELETE SET NULL,
    event_type  VARCHAR(100),
    location    VARCHAR(255),
    product_id  INT          REFERENCES Products(id) ON DELETE SET NULL,
    seller_id   INT,   -- BE lookup, không FK cứng
    category_id INT,   -- BE lookup, không FK cứng
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── Conversations ──────────────────────────────────────────
CREATE TABLE Conversations (
    id              SERIAL PRIMARY KEY,
    product_id      INT         NOT NULL REFERENCES Products(id) ON DELETE CASCADE,
    buyer_id        INT         NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    seller_id       INT         NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    external_ref    VARCHAR(255),  -- Zalo ID
    status          VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'closed', 'cancelled')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_message_at TIMESTAMPTZ
);

-- ─── Orders ─────────────────────────────────────────────────
CREATE TABLE Orders (
    id                  SERIAL PRIMARY KEY,
    conversation_id     INT            REFERENCES Conversations(id) ON DELETE SET NULL,
    buyer_id            INT            NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    seller_id           INT            NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    shop_id             INT            REFERENCES Shops(id) ON DELETE SET NULL,
    product_id          INT            NOT NULL REFERENCES Products(id) ON DELETE CASCADE,
    deal_amount         DECIMAL(15, 2) NOT NULL,
    deposit_amount      DECIMAL(15, 2),
    status              VARCHAR(20)    NOT NULL DEFAULT 'pending'
                            CHECK (status IN ('pending', 'deposited', 'shipped', 'confirmed', 'cancelled')),
    shipped_at          TIMESTAMPTZ,
    buyer_confirmed_at  TIMESTAMPTZ,
    auto_release_at     TIMESTAMPTZ,
    ordered_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- ─── Escrow ─────────────────────────────────────────────────
CREATE TABLE Escrow (
    id               SERIAL PRIMARY KEY,
    order_id         INT            NOT NULL UNIQUE REFERENCES Orders(id) ON DELETE CASCADE,
    amount           DECIMAL(15, 2) NOT NULL,
    status           VARCHAR(20)    NOT NULL DEFAULT 'holding'
                         CHECK (status IN ('holding', 'released', 'refunded')),
    release_type     VARCHAR(30)    CHECK (release_type IN ('buyer_confirm', 'auto_release', 'cancelled')),
    hold_at          TIMESTAMPTZ,
    release_at       TIMESTAMPTZ,
    auto_release_at  TIMESTAMPTZ
);

-- ─── Payments ───────────────────────────────────────────────
CREATE TABLE Payments (
    id           SERIAL PRIMARY KEY,
    order_id     INT            NOT NULL REFERENCES Orders(id) ON DELETE CASCADE,
    user_id      INT            NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    type         VARCHAR(20)    NOT NULL CHECK (type IN ('deposit', 'refund', 'payout', 'boost')),
    provider     VARCHAR(20)    CHECK (provider IN ('vnpay', 'momo')),
    provider_ref VARCHAR(255),
    amount       DECIMAL(15, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'success', 'failed')),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    paid_at      TIMESTAMPTZ
);

-- ─── Payment_Logs ───────────────────────────────────────────
CREATE TABLE Payment_Logs (
    id           SERIAL PRIMARY KEY,
    payment_id   INT         NOT NULL REFERENCES Payments(id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL CHECK (status IN ('pending', 'success', 'failed')),
    raw_response JSONB,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ─── Notifications ──────────────────────────────────────────
CREATE TABLE Notifications (
    id        SERIAL PRIMARY KEY,
    user_id   INT         NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    order_id  INT         REFERENCES Orders(id) ON DELETE SET NULL,
    type      VARCHAR(50) NOT NULL
                  CHECK (type IN ('order_request', 'confirm_reminder', 'auto_release', 'payment')),
    message   TEXT        NOT NULL,
    is_read   BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    sent_at   TIMESTAMPTZ
);

-- ─── Review_Tags ────────────────────────────────────────────
CREATE TABLE Review_Tags (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20)  NOT NULL CHECK (type IN ('buyer', 'seller'))
);

-- ─── Reviews ────────────────────────────────────────────────
CREATE TABLE Reviews (
    id            SERIAL PRIMARY KEY,
    order_id      INT            NOT NULL REFERENCES Orders(id) ON DELETE CASCADE,
    reviewer_id   INT            NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    seller_id     INT            NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    shop_id       INT            REFERENCES Shops(id) ON DELETE SET NULL,
    rating        DECIMAL(2, 1)  NOT NULL CHECK (rating >= 1 AND rating <= 5),
    tags          JSONB,
    content       TEXT,
    reviewer_role VARCHAR(20)    NOT NULL CHECK (reviewer_role IN ('buyer', 'seller')),
    status        VARCHAR(20)    NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'hidden')),
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- ─── Indexes (tối ưu query phổ biến) ───────────────────────
CREATE INDEX idx_products_user_id     ON Products(user_id);
CREATE INDEX idx_products_shop_id     ON Products(shop_id);
CREATE INDEX idx_products_category_id ON Products(category_id);
CREATE INDEX idx_products_status      ON Products(status);
CREATE INDEX idx_orders_buyer_id      ON Orders(buyer_id);
CREATE INDEX idx_orders_seller_id     ON Orders(seller_id);
CREATE INDEX idx_orders_status        ON Orders(status);
CREATE INDEX idx_conversations_buyer  ON Conversations(buyer_id);
CREATE INDEX idx_conversations_seller ON Conversations(seller_id);
CREATE INDEX idx_notifications_user   ON Notifications(user_id, is_read);
CREATE INDEX idx_sessions_user_id     ON Sessions(user_id);
