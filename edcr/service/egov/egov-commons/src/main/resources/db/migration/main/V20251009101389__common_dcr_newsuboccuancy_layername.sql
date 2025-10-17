INSERT INTO egbpa_sub_occupancy (
    id, code, name, ordernumber, isactive, createdby, createddate,
    lastmodifieddate, lastmodifiedby, version, maxfar, occupancy,
    description, colorcode
) VALUES 
(
    nextval('seq_egbpa_sub_occupancy'), 'F-HB', 'Hotels with Banquet halls',
    (SELECT MAX(ordernumber) + 1 FROM egbpa_sub_occupancy), 't', 1, NOW(), NOW(),
    1, 0, 0.5, (SELECT id FROM egbpa_occupancy WHERE code = 'F'),
    'Hotels with Banquet halls', 70
);

INSERT INTO egbpa_sub_occupancy (
    id, code, name, ordernumber, isactive, createdby, createddate,
    lastmodifieddate, lastmodifiedby, version, maxfar, occupancy,
    description, colorcode
) VALUES 
(
    nextval('seq_egbpa_sub_occupancy'), 'F-HWB', 'Hotels without Banquet halls',
    (SELECT MAX(ordernumber) + 1 FROM egbpa_sub_occupancy), 't', 1, NOW(), NOW(),
    1, 0, 0.5, (SELECT id FROM egbpa_occupancy WHERE code = 'F'),
    'Hotels without Banquet halls', 71
);

INSERT INTO egbpa_sub_occupancy (
    id, code, name, ordernumber, isactive, createdby, createddate,
    lastmodifieddate, lastmodifiedby, version, maxfar, occupancy,
    description, colorcode
) VALUES 
(
    nextval('seq_egbpa_sub_occupancy'), 'F-PB', 'Private business / Business office',
    (SELECT MAX(ordernumber) + 1 FROM egbpa_sub_occupancy), 't', 1, NOW(), NOW(),
    1, 0, 0.5, (SELECT id FROM egbpa_occupancy WHERE code = 'F'),
    'Private business / Business office', 72
);

INSERT INTO egbpa_occupancy (
    id, code, name, isactive, version, createdby, createddate,
    lastmodifiedby, lastmodifieddate, maxcoverage, minfar, maxfar,
    ordernumber, description, colorcode
) VALUES 
(
    nextval('seq_egbpa_occupancy'), 'K', 'Factories', 't', 0, 1, NOW(), 1, NOW(),
    65, 3, 4, 1, 'Factories', 30
);
