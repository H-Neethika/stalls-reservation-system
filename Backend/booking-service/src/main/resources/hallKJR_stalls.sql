
-- ============================================================
-- HALL K (hall_id = 7)
-- ============================================================

-- SMALL STALLS
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S374', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S374');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S375', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S375');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S376', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S376');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S377', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S377');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S378', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S378');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S379', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S379');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S380', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S380');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S381', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S381');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S382', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S382');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S383', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S383');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S384', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S384');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S385', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S385');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S386', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S386');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S387', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S387');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S388', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S388');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S389', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S389');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S390', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S390');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S391', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S391');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S392', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S392');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S393', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S393');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S394', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S394');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S395', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S395');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S396', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S396');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S397', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S397');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S398', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S398');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S399', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S399');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S400', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S400');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S401', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S401');
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'S402', 7, 'SMALL'               WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='S402');

-- LARGE STALLS
INSERT INTO stall (stall_name, hall_id, stall_type               )
SELECT 'L410', 7, 'LARGE'                 WHERE NOT EXISTS (SELECT 1 FROM stall WHERE stall_name='L410');




-- ============================================================
-- HALL J (hall_id = 8)
-- ============================================================

-- SMALL STALLS
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S302',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S302');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S303',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S303');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S304',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S304');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S305',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S305');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S306',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S306');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S307',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S307');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S308',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S308');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S309',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S309');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S310',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S310');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S311',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S311');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S312',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S312');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S313',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S313');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S314',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S314');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S315',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S315');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S316',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S316');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S317',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S317');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S318',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S318');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S319',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S319');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S320',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S320');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S321',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S321');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S322',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S322');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S323',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S323');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S324',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S324');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S325',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S325');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S326',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S326');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S327',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S327');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S328',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S328');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S329',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S329');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S330',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S330');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S331',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S331');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S332',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S332');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S333',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S333');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S334',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S334');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S335',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S335');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S336',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S336');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S337',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S337');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'S338',8,'SMALL'                   WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='S338');

-- MEDIUM STALLS
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M339',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M339');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M340',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M340');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M341',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M341');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M342',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M342');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M343',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M343');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M344',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M344');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M345',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M345');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M346',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M346');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M347',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M347');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M348',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M348');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M349',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M349');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M350',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M350');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M351',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M351');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M352',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M352');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M353',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M353');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M354',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M354');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M355',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M355');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M356',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M356');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M357',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M357');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M358',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M358');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M359',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M359');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M360',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M360');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M361',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M361');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M362',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M362');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M363',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M363');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M364',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M364');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M365',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M365');
INSERT INTO stall (stall_name,hall_id,stall_type ) SELECT 'M366',8,'MEDIUM'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='M366');



-- ============================================================
-- HALL R (hall_id = 9)
-- ============================================================
INSERT INTO stall (stall_name,hall_id,stall_type )
SELECT 'L-R',9,'LARGE'  WHERE NOT EXISTS(SELECT 1 FROM stall WHERE stall_name='L-R');

