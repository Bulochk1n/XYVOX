
CREATE TABLE IF NOT EXISTS Role (
                                    RoleID    INT AUTO_INCREMENT PRIMARY KEY,
                                    Role_Name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Wallet (
                                      id INT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS Appuser (
                                       UserID     INT AUTO_INCREMENT PRIMARY KEY,
                                       Email      VARCHAR(100) NOT NULL UNIQUE,
                                       Username   VARCHAR(100) NOT NULL UNIQUE,
                                       Password   VARCHAR(150) NOT NULL,
                                       Enabled    BOOLEAN       NOT NULL DEFAULT TRUE,
                                       role_id    INT NOT NULL,
                                       wallet_id  INT UNIQUE,
                                       CONSTRAINT FK_Appuser_Role   FOREIGN KEY (role_id)   REFERENCES Role(RoleID),
                                       CONSTRAINT FK_Appuser_Wallet FOREIGN KEY (wallet_id) REFERENCES Wallet(id)
);


CREATE TABLE IF NOT EXISTS Coin (
                                    id               INT AUTO_INCREMENT PRIMARY KEY,
                                    Symbol           VARCHAR(50)  NOT NULL UNIQUE,
                                    Name             VARCHAR(100) NOT NULL UNIQUE,
                                    Network          VARCHAR(50)  NOT NULL,
                                    Decimals         INT          NOT NULL,
                                    Price            DOUBLE       NOT NULL,
                                    Last_Updated_At  TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS wallet_coin (
                                           id         INT AUTO_INCREMENT PRIMARY KEY,
                                           wallet_id  INT NOT NULL,
                                           coin_id    INT NOT NULL,
                                           amount     DOUBLE NOT NULL,
                                           coinvalue      DOUBLE NOT NULL,
                                           CONSTRAINT UK_WalletCoin UNIQUE (wallet_id, coin_id),
                                           CONSTRAINT FK_WalletCoin_Wallet FOREIGN KEY (wallet_id) REFERENCES Wallet(id),
                                           CONSTRAINT FK_WalletCoin_Coin   FOREIGN KEY (coin_id)   REFERENCES Coin(id)
);

CREATE TABLE news_items (
                            id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                            source        VARCHAR(128)  NOT NULL,
                            title         VARCHAR(512)  NOT NULL,
                            link          VARCHAR(1024) NOT NULL,
                            pub_date      TIMESTAMP     NOT NULL,
                            summary       text
);

CREATE INDEX idx_news_pubdate ON news_items (pub_date DESC);