insert into state.egdcr_layername(id,key,value,createdby,createddate,lastmodifiedby,lastmodifieddate,version)
select nextval('state.seq_egdcr_layername'),'LAYER_NAME_RAINWATER_HARWESTING_PERCOLATION_PIT','RWH_PERCOLATION_PIT',1,now(),1,now(),0
where not exists(select key from state.egdcr_layername where key='LAYER_NAME_RAINWATER_HARWESTING_PERCOLATION_PIT');