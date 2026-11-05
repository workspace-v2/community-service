-- 개발/검증용 합성 데이터. 실제 메뉴나 실제 리뷰가 아니다. 재실행하면 행이 추가된다.
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO brand(name,created_at,updated_at) VALUES('데모 버거 연구소',NOW(6),NOW(6));
SET @demo_brand_id=LAST_INSERT_ID();
INSERT INTO menu(brand_id,name,description,price,source_type,created_at,updated_at) VALUES
(@demo_brand_id,'데모 더블 치즈버거','두꺼운 소고기 패티와 치즈를 넣은 합성 메뉴',6900,'SYNTHETIC',NOW(6),NOW(6)),
(@demo_brand_id,'데모 매콤 치킨버거','바삭한 치킨 패티와 매콤한 소스를 넣은 합성 메뉴',5900,'SYNTHETIC',NOW(6),NOW(6)),
(@demo_brand_id,'데모 프리미엄 버거','육즙이 풍부한 패티를 넣은 합성 메뉴',10900,'SYNTHETIC',NOW(6),NOW(6));
COMMIT;
