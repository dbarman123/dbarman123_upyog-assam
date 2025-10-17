package org.egov.land.repository.rowmapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.egov.land.web.models.Address;
import org.egov.land.web.models.AuditDetails;
import org.egov.land.web.models.Boundary;
import org.egov.land.web.models.Channel;
import org.egov.land.web.models.Document;
import org.egov.land.web.models.GeoLocation;
import org.egov.land.web.models.Institution;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.OwnerInfoV2;
import org.egov.land.web.models.Unit;
import org.egov.land.web.models.Source;
import org.egov.land.web.models.Status;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class LandRowMapper implements ResultSetExtractor<List<LandInfo>> {

	@Override
	public List<LandInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {

		Map<String, LandInfo> landMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("land_id");
			if (id == null) continue;

			LandInfo currentLandInfo = landMap.get(id);
			String tenantId = rs.getString("land_tenant_id");

			if (currentLandInfo == null) {

				// created / lastModified times
				Long createdTime = rs.getLong("land_created_time");
				if (rs.wasNull()) createdTime = null;
				Long lastModifiedTime = rs.getLong("land_last_modified_time");
				if (rs.wasNull()) lastModifiedTime = null;

				// additional details JSON
				String additionalDetailsStr = rs.getString("land_additional_details");
				Object additionalDetails = null;
				if (additionalDetailsStr != null && !additionalDetailsStr.equals("{}") && !additionalDetailsStr.equals("null")) {
					additionalDetails = new Gson().fromJson(additionalDetailsStr, Object.class);
				}

				AuditDetails auditdetails = AuditDetails.builder()
						.createdBy(rs.getString("land_created_by"))
						.createdTime(createdTime)
						.lastModifiedBy(rs.getString("land_last_modified_by"))
						.lastModifiedTime(lastModifiedTime)
						.build();

				// geolocation
				GeoLocation geoLocation = GeoLocation.builder()
						.id(rs.getString("geo_id"))
						.latitude((Double) rs.getObject("latitude"))
						.longitude((Double) rs.getObject("longitude"))
						.build();

				Boundary locality = Boundary.builder().code(rs.getString("locality")).build();

				// address
				Address address = Address.builder()
						.id(rs.getString("land_address_id"))
						.houseNo(rs.getString("house_no"))
						.addressLine1(rs.getString("address_line_1"))
						.addressLine2(rs.getString("address_line_2"))
						.landmark(rs.getString("landmark"))
						.district(rs.getString("district"))
						.region(rs.getString("region"))
						.state(rs.getString("state"))
						.country(rs.getString("country"))
						.pincode(rs.getString("pincode"))
						.geoLocation(geoLocation)
						.tenantId(tenantId)
						.locality(locality)
						.build();

				// build LandInfo
				currentLandInfo = LandInfo.builder()
						.id(id)
						.landUId(rs.getString("land_uid"))
						.landUniqueRegNo(rs.getString("land_regno"))
						.oldDagNumber(rs.getString("old_dag_no"))
						.newDagNumber(rs.getString("new_dag_no"))
						.oldPattaNumber(rs.getString("old_patta_no"))
						.newPattaNumber(rs.getString("new_patta_no"))
						.totalPlotArea(rs.getBigDecimal("total_plot_area"))
						.tenantId(tenantId)
						.status(rs.getString("land_status") != null ? Status.fromValue(rs.getString("land_status")) : null)
						.address(address)
						.ownershipCategory(rs.getString("ownership_category"))
						.source(rs.getString("source") != null ? Source.fromValue(rs.getString("source")) : null)
						.channel(rs.getString("channel") != null ? Channel.fromValue(rs.getString("channel")) : null)
						.auditDetails(auditdetails)
						.additionalDetails(additionalDetails)
						.build();

				landMap.put(id, currentLandInfo);
			}

			addChildrenToProperty(rs, currentLandInfo);
		}

		return new ArrayList<>(landMap.values());
	}

	private void addChildrenToProperty(ResultSet rs, LandInfo landInfo) throws SQLException {

		String tenantId = landInfo.getTenantId();

		AuditDetails auditdetails = AuditDetails.builder()
				.createdBy(rs.getString("land_created_by"))
				.createdTime(rs.getLong("land_created_time"))
				.lastModifiedBy(rs.getString("land_last_modified_by"))
				.lastModifiedTime(rs.getLong("land_last_modified_time"))
				.build();

		// Unit - check if already exists
		String unitId = rs.getString("unit_id");
		if (unitId != null && !isUnitAlreadyAdded(landInfo, unitId)) {
			Long occupancyDate = null;
			long occ = rs.getLong("occupancy_date");
			if (!rs.wasNull()) occupancyDate = occ;

			Unit unit = Unit.builder()
					.id(unitId)
					.floorNo(rs.getString("floor_no"))
					.unitType(rs.getString("unit_type"))
					.usageCategory(rs.getString("usage_category"))
					.occupancyType(rs.getString("occupancy_type"))
					.occupancyDate(occupancyDate)
					.auditDetails(auditdetails)
					.tenantId(tenantId)
					.build();
			landInfo.addUnitItem(unit);
		}

		// Owner - check if already exists
		String ownerId = rs.getString("owner_id");
		if (ownerId != null) {
			// Try to get existing owner
			OwnerInfoV2 owner = landInfo.getOwners() != null
					? landInfo.getOwners().stream()
					.filter(o -> ownerId.equals(o.getOwnerId()))
					.findFirst()
					.orElse(null)
					: null;

			if (owner == null) {
				owner = OwnerInfoV2.builder()
						.tenantId(tenantId)
						.ownerId(ownerId)
						.uuid(rs.getString("owner_uuid"))
						.isPrimaryOwner((Boolean) rs.getObject("is_primary_owner"))
						.ownerShipPercentage(rs.getBigDecimal("ownership_percentage"))
						.institutionId(rs.getString("owner_institution_id"))
						.motherName(rs.getString("owner_mother_name"))
						.status((Boolean) rs.getObject("owner_status"))
						.auditDetails(auditdetails)
						.build();

				landInfo.addOwnerItem(owner);
			}

			// Owner Address
			String ownerAddressId = rs.getString("owner_info_id");
			String addressType = rs.getString("owner_address_type");

			if (ownerAddressId != null && "PERMANENT_ADDRESS".equals(addressType)) {
				Address permanentAddress = Address.builder()
						.id(rs.getString("owner_address_id"))
						.ownerInfoId(ownerAddressId)
						.houseNo(rs.getString("owner_house_no"))
						.addressLine1(rs.getString("owner_address_line1"))
						.addressLine2(rs.getString("owner_address_line2"))
						.landmark(rs.getString("owner_landmark"))
						.district(rs.getString("owner_district"))
						.localityCode(rs.getString("owner_locality"))
						.region(rs.getString("owner_region"))
						.state(rs.getString("owner_state"))
						.country(rs.getString("owner_country"))
						.pincode(rs.getString("owner_pincode"))
						.addressType(addressType)
						.tenantId(tenantId)
						.build();
				owner.setPermanentAddress(permanentAddress);
			}

			if (ownerAddressId != null && "CORRESPONDENCE_ADDRESS".equals(addressType)) {
				Address correspondenceAddress = Address.builder()
						.id(rs.getString("owner_address_id"))
						.ownerInfoId(ownerAddressId)
						.houseNo(rs.getString("owner_house_no"))
						.addressLine1(rs.getString("owner_address_line1"))
						.addressLine2(rs.getString("owner_address_line2"))
						.landmark(rs.getString("owner_landmark"))
						.localityCode(rs.getString("owner_locality"))
						.district(rs.getString("owner_district"))
						.region(rs.getString("owner_region"))
						.state(rs.getString("owner_state"))
						.country(rs.getString("owner_country"))
						.pincode(rs.getString("owner_pincode"))
						.addressType(addressType)
						.tenantId(tenantId)
						.build();
				owner.setCorrespondenceAddress(correspondenceAddress);
			}
		}

		// Institution - only set if not already set
		String instId = rs.getString("inst_id");
		if (instId != null && landInfo.getInstitution() == null) {
			Institution institution = Institution.builder()
					.id(instId)
					.type(rs.getString("inst_type"))
					.designation(rs.getString("inst_designation"))
					.nameOfAuthorizedPerson(rs.getString("inst_authorized_person"))
					.tenantId(tenantId)
					.build();
			landInfo.setInstitution(institution);
		}

		// Document - check if already exists
		String documentId = rs.getString("doc_id");
		if (documentId != null && !isDocumentAlreadyAdded(landInfo, documentId)) {
			Document document = Document.builder()
					.id(documentId)
					.documentType(rs.getString("doc_type"))
					.fileStoreId(rs.getString("doc_filestore"))
					.documentUid(rs.getString("doc_uid"))
					.auditDetails(auditdetails)
					.build();
			landInfo.addDocumentItem(document);
		}
	}

	/// Helper Methods to check if owners, units & Docs existed before
	private boolean isUnitAlreadyAdded(LandInfo landInfo, String unitId) {
		return landInfo.getUnits() != null &&
				landInfo.getUnits().stream().anyMatch(unit -> unitId.equals(unit.getId()));
	}

	private boolean isOwnerAlreadyAdded(LandInfo landInfo, String ownerId) {
		return landInfo.getOwners() != null &&
				landInfo.getOwners().stream().anyMatch(owner -> ownerId.equals(owner.getOwnerId()));
	}

	private boolean isDocumentAlreadyAdded(LandInfo landInfo, String documentId) {
		return landInfo.getDocuments() != null &&
				landInfo.getDocuments().stream().anyMatch(doc -> documentId.equals(doc.getId()));
	}


}
