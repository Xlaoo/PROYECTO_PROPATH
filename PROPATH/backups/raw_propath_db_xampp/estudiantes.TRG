TYPE=TRIGGERS
triggers='CREATE DEFINER=`root`@`localhost` TRIGGER trg_estudiantes_nombres_insert\nBEFORE INSERT ON estudiantes\nFOR EACH ROW\nBEGIN\n    SET NEW.nombres = (\n        SELECT u.nombre\n        FROM usuarios u\n        WHERE u.id_usuario = NEW.id_usuario\n        LIMIT 1\n    );\nEND'
sql_modes=1098907648
definers='root@localhost'
client_cs_names='utf8mb4'
connection_cl_names='utf8mb4_unicode_ci'
db_cl_names='utf8mb4_unicode_ci'
created=1792652282239811
