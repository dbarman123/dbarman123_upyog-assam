insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_ROOM_WINDOW',
       'BLK_%s_FLR_%s_UNIT_%s_REGULAR_ROOM_%s_WINDOW_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_ROOM_WINDOW'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_WINDOW',
       'BLK_%s_FLR_%s_UNIT_%s_WINDOW_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_WINDOW'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNITWISE_REGULAR_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_REGULAR_ROOM_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNITWISE_REGULAR_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_WINDOW',
       'BLK_%s_FLR_%s_UNIT_%s_WINDOW_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_WINDOW'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_REGULAR_ROOM_DOOR',
       'BLK_%s_FLR_%s_UNIT_%s_REGULAR_ROOM_%s_DOOR_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_REGULAR_ROOM_DOOR'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_VERANDAH',
       'BLK_%s_FLR_%s_UNIT_%s_VERANDAH',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_VERANDAH'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_ROOM_WINDOW',
       'BLK_%s_FLR_%s_UNIT_%s_REGULAR_ROOM_%s_WINDOW_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_ROOM_WINDOW'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_WINDOW',
       'BLK_%s_FLR_%s_UNIT_%s_WINDOW_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_WINDOW'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_BLK_FLR_UNIT_TOILET',
       'BLK_%s_FLR_%s_UNIT_%s_TOILET_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_BLK_FLR_UNIT_TOILET'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_BLK_FLR_UNIT_TOILET_VENTILATION',
       'BLK_%s_FLR_%s_UNIT_%s_TOILET_%s_VENTILATION',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_BLK_FLR_UNIT_TOILET_VENTILATION'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_REGULAR_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_REGULAR_ROOM',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_REGULAR_ROOM'
);

insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT',
       'UNIT',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_MEZZANINE_AT_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_ROOM_%s_MEZ_AREA_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_MEZZANINE_AT_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_MEZZANINE_AT_ACROOM',
       'BLK_%s_FLR_%s_UNIT_%s_ACROOM_%s_MEZ_AREA_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_MEZZANINE_AT_ACROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_AC_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_AC_ROOM_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_AC_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_AC_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_AC_ROOM_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_AC_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_NON_INHABITABLE_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_NON_INHABITABLE_ROOM_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_NON_INHABITABLE_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_MEZZANINE_AT_NON_INHABITABLE_ROOM',
       'BLK_%s_FLR_%s_UNIT_%s_ROOM_%s_NONINHABITABLE_ROOM_MEZ_AREA_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_MEZZANINE_AT_NON_INHABITABLE_ROOM'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_HILLY_ROOM_HEIGHT',
       'BLK_%s_FLR_%s_UNIT_%s_HILLY_ROOM_HEIGHT_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_HILLY_ROOM_HEIGHT'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_DOOR',
       'BLK_%s_FLR_%s_UNIT_%s_DOOR_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_DOOR'
);
insert into state.egdcr_layername(id, "key", value, createdby, createddate, lastmodifiedby, lastmodifieddate, version)
select nextval('state.seq_egdcr_layername'),
       'LAYER_NAME_UNIT_NON_HABITATIONAL_DOOR',
       'BLK_%s_FLR_%s_UNIT_%s_NON_HABITATIONAL_DOOR_%s',
       1,
       now(),
       1,
       now(),
       0
where not exists (
    select 1 from state.egdcr_layername where "key" = 'LAYER_NAME_UNIT_NON_HABITATIONAL_DOOR'
);
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_BALCONY','BLK_%s_FLR_%s_UNIT_%s_BALCONY_&s',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_BALCONY');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_NAME_PREFIX','UNIT_',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_NAME_PREFIX');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_BLK_FLR_UNIT_BATH','BLK_%s_FLR_%s_UNIT_%s_BATH',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_BLK_FLR_UNIT_BATH');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_BLK_FLR_UNIT_BATH_HT','BLK_%s_FLR_%s_UNIT_%s_BATH_HT',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_BLK_FLR_UNIT_BATH_HT');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_KITCHEN','BLK_%s_FLR_%s_UNIT_%s_KITCHEN',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_KITCHEN');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_LIGHT_VENTILATION','BLK_%s_FLR_%s_UNIT_%s_LIGHT_VENTILATION',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_LIGHT_VENTILATION');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_ROOM_LIGHT_VENTILATION','BLK_%s_FLR_%s_UNIT_%s_ROOM_%s',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_ROOM_LIGHT_VENTILATION');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_ACROOM_LIGHT_VENTILATION','BLK_%s_FLR_%s_UNIT_%s_ACROOM_LIGHT_VENTILATION',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_ACROOM_LIGHT_VENTILATION');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_KITCHEN_DINING_VENTILATION','BLK_%s_FLR_%s_UNIT_%s_KITCHEN_DINING_VENTILATION',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_KITCHEN_DINING_VENTILATION');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_LAUNDRY_RECREATION_VENTILATION','BLK_%s_FLR_%s_UNIT_%s_LAUNDRY_RECREATION_VENTILATION',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_LAUNDRY_RECREATION_VENTILATION');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_BLK_FLR_UNIT_WC_BATH','BLK_%s_FLR_%s_UNIT_%s_WC_BATH',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_BLK_FLR_UNIT_WC_BATH');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_BLK_FLR_UNIT_WC_BATH_HT','BLK_%s_FLR_%s_UNIT_%s_WC_BATH_HT',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_BLK_FLR_UNIT_WC_BATH_HT');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_UNIT_SERVICEROOM','BLK_%s_FLR_%s_UNIT_%s_SERVICEROOM',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_UNIT_SERVICEROOM');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_BLK_FLR_UNIT_WC_HT','BLK_%s_FLR_%s_UNIT_%s_WC_HT',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_BLK_FLR_UNIT_WC_HT');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_BLK_FLR_UNIT_COMMON_ROOM_HT','BLK_%s_FLR_%s_UNIT_%s_COMMON_ROOM_HT',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_BLK_FLR_UNIT_COMMON_ROOM_HT');
insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version) select nextval('state.seq_egdcr_layername'),'LAYER_NAME_DA_RAMP_LANDING','BLK_%s_DA_RAMP_LANDING',1,now(),1,now(),0 where not exists(select key from state.egdcr_layername where key='LAYER_NAME_DA_RAMP_LANDING');
