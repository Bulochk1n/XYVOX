
INSERT INTO Role (Role_Name) VALUES
                                 ('ROLE_ADMIN'),
                                 ('ROLE_MODERATOR'),
                                 ('ROLE_USER');


INSERT INTO Wallet () VALUES (), (), (), ();


INSERT INTO Appuser (Username, Password, Email, Enabled, Role_ID, Wallet_ID) VALUES
                                                                                 ('admin',     '{bcrypt}$2a$12$.2APviwLGqkqNpR5a2fcReKcyo7F0eCJefzZZt.KR4c8n4qh8FxGq',
                                                                                  'admin@example.com',     true, 1, 1),
                                                                                 ('moderator', '{bcrypt}$2a$12$LytmAln8qJY.ErzundartuvmvIof3jiaJpKAZQF5VIXulJtfKzcF6',
                                                                                  'moderator@example.com', true, 2, 2),
                                                                                 ('danik',     '{bcrypt}$2a$12$twArH0dj.3pQrY7z45Bw2.YdW.AElJyx3KsUzOVgbdA6QL.aRiEGq',
                                                                                  'danik@example.com',     true, 3, 3),
                                                                                 ('bob',       '{bcrypt}$2a$12$Ty.Brfqu4Xji2vk3DMixz.dRDuWMYOAm0iiaDF.QAJpFgKXf2utfO',
                                                                                  'bob@example.com',       true, 3, 4);

INSERT INTO Coin (Symbol, Name, Network, Decimals, Price, Last_Updated_At) VALUES
                                                                               ('BTC', 'Bitcoin',  'Bitcoin',  8,  60000.00, NOW()),
                                                                               ('ETH', 'Ethereum', 'Ethereum', 8,   4000.00, NOW()),
                                                                               ('LTC', 'Litecoin','Litecoin',  8,    200.00, NOW()),
                                                                               ('USDT', 'Tether',         'Ethereum', 6, 1.00,    NOW()),
                                                                               ('BNB',  'Binance Coin',   'BSC',      8, 600.00,  NOW()),
                                                                               ('SOL',  'Solana',         'Solana',   8, 150.00,  NOW()),
                                                                               ('XRP',  'Ripple',         'XRP',      6, 0.60,    NOW()),
                                                                               ('ADA',  'Cardano',        'Cardano',  6, 0.50,    NOW()),
                                                                               ('DOGE', 'Dogecoin',       'Dogecoin', 8, 0.10,    NOW()),
                                                                               ('DOT',  'Polkadot',       'Polkadot', 8, 7.00,    NOW()),
                                                                               ('TRX',  'Tron',           'Tron',     6, 0.08,    NOW()),
                                                                               ('AVAX', 'Avalanche',      'Avalanche',8, 35.00,   NOW());

INSERT INTO wallet_coin (wallet_id, coin_id, amount, coinvalue) VALUES
                                                                    (1,  1,   0.12345678,  7407.4068),
                                                                    (1,  2,   2.50000000, 10000.0000),
                                                                    (1,  4,1000.000000,   1000.0000),
                                                                    (1,  5,  10.000000,   6000.0000),
                                                                    (1,  6,  25.000000,   3750.0000),
                                                                    (1,  7,1000.000000,    600.0000),
                                                                    (1,  8,2000.000000,   1000.0000),
                                                                    (1,  9,5000.000000,    500.0000),
                                                                    (1, 10,  50.000000,    350.0000),
                                                                    (1, 11,3000.000000,    240.0000),
                                                                    (1, 12,  15.000000,    525.0000),

                                                                    (2,  2,   1.00000000,  4000.0000),
                                                                    (2,  3,  10.00000000,  2000.0000),

                                                                    (3,  1,   0.00500000,   300.0000),
                                                                    (3,  3,   5.25000000,  1050.0000),

                                                                    (4,  2,   0.75000000,  3000.0000),
                                                                    (4,  3,   1.50000000,   300.0000);


-- 1) CoinDesk
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('CoinDesk RSS',
     'VivoPower to Deploy $100M in XRP on Flare, Add Ripple USD for Treasury Operations',
     'https://www.coindesk.com/business/2025/06/11/vivopower-to-deploy-usd100m-in-xrp-on-flare-add-ripple-usd-for-treasury-operations',
     '2025-06-09 14:23:00',
     'The Nasdaq-listed firm, which recently adopted an XRP-focused treasury strategy, aims to generate yield on its digital asset holdings via Flare.');

-- 2) Cointelegraph
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('Cointelegraph RSS',
     'Bitcoin Pushes Toward $107K Even as Trump Sends National Guard to Los Angeles',
     'https://www.coindesk.com/markets/2025/06/08/bitcoin-pushes-toward-usd107k-even-as-trump-sends-national-guard-to-los-angeles',
     '2025-06-08 13:45:00',
     'Bitcoin gains 0.78% despite a tense political backdrop in the U.S., with markets shrugging off fears of further unrest and a potential military mobilization.');

-- 3) Binance Blog
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('Binance Blog RSS',
     'Binance Launches New Staking Program with 12% APY',
     'https://www.binance.com/en/blog/421499824684905600/Binance-Launches-New-Staking-Program-with-12-APY',
     '2025-06-06 12:00:00',
     'Binance announces a new staking initiative offering up to 12% APY for select PoS tokens.');

-- 4) CoinDesk
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('CoinDesk RSS',
     'Regulators Eye Stricter Crypto Rules as Market Matures',
     'https://www.coindesk.com/policy/2025/06/05/regulators-eye-stricter-crypto-rules-as-market-matures/',
     '2025-06-05 10:15:00',
     'Global regulators are discussing tougher regulations for stablecoins and DeFi platforms.');

-- 5) Cointelegraph
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('Cointelegraph RSS',
     'Cardano Smart Contracts Go Live, Catalyzing New DApp Growth',
     'https://cointelegraph.com/news/cardano-smart-contracts-go-live-catalyzing-new-dapp-growth',
     '2025-06-04 09:30:00',
     'With smart contract functionality activated on Cardano, developers rush to build new decentralized apps.');

-- 6) CoinDesk
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('CoinDesk RSS',
     'SEC Delays Decision on Ethereum ETF Amid Industry Feedback',
     'https://www.coindesk.com/markets/2025/06/03/sec-delays-decision-on-ethereum-etf-amid-industry-feedback/',
     '2025-06-03 11:00:00',
     'The SEC has pushed back its verdict on a spot Ether ETF after receiving pushback from stakeholders.');

-- 7) Cointelegraph
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('Cointelegraph RSS',
     'Polkadot’s Rococo Testnet Reaches 1 Million Transactions',
     'https://cointelegraph.com/news/polkadots-rococo-testnet-reaches-1-million-transactions',
     '2025-06-02 16:20:00',
     'The Rococo testnet on Polkadot has processed over one million transactions, showcasing scalability.');

-- 8) Binance Blog
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('Binance Blog RSS',
     'Binance USD (BUSD) Listed on Multiple New Markets',
     'https://www.binance.com/en/blog/421343210567123456/BUSD-Listed-on-Multiple-New-Markets',
     '2025-06-02 14:45:00',
     'Binance adds support for BUSD trading pairs on four additional regional exchanges.');

-- 9) CoinDesk
INSERT INTO news_items (source, title, link, pub_date, summary) VALUES
    ('CoinDesk RSS',
     'BlockFi Emerges from Bankruptcy, Relaunches Lending Services',
     'https://www.coindesk.com/markets/2025/06/01/blockfi-emerges-from-bankruptcy-relaunches-lending-services/',
     '2025-06-01 13:10:00',
     'BlockFi has exited bankruptcy and is back online, offering crypto lending and borrowing services.');




