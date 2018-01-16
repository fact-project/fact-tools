package fact.utils;

import org.junit.Test;
import stream.Data;
import stream.data.DataFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SanitizeKeyTest {

    @Test
    public void testBasics() {
        assertEquals("camel_case", SanitizeKeys.renameKey("CamelCase"));
        assertEquals("camel_case2", SanitizeKeys.renameKey("CamelCase2"));
        assertEquals("fact_event", SanitizeKeys.renameKey("FACTEvent"));
    }

    @Test
    public void testFalsePositive() {
        assertEquals("cog_x", SanitizeKeys.renameKey("cog_x"));
        assertEquals("pointing_position_az", SanitizeKeys.renameKey("pointing_position_az"));
        assertEquals("num_pixel_in_shower", SanitizeKeys.renameKey("num_pixel_in_shower"));
    }

    @Test
    public void testMCKeys() {
        assertEquals("corsika_event_header_first_interaction_height", SanitizeKeys.renameKey("CorsikaEvtHeader.fFirstInteractionHeight"));
        assertEquals("incident_angle", SanitizeKeys.renameKey("IncidentAngle.fVal"));
        assertEquals("corsika_event_header_az", SanitizeKeys.renameKey("MCorsikaEvtHeader.fAz"));
        assertEquals("corsika_event_header_event_number", SanitizeKeys.renameKey("MCorsikaEvtHeader.fEvtNumber"));
        assertEquals("corsika_event_header_first_target_num", SanitizeKeys.renameKey("MCorsikaEvtHeader.fFirstTargetNum"));
        assertEquals("corsika_event_header_momentum_x", SanitizeKeys.renameKey("MCorsikaEvtHeader.fMomentumX"));
        assertEquals("corsika_event_header_momentum_y", SanitizeKeys.renameKey("MCorsikaEvtHeader.fMomentumY"));
        assertEquals("corsika_event_header_momentum_z", SanitizeKeys.renameKey("MCorsikaEvtHeader.fMomentumZ"));
        assertEquals("corsika_event_header_num_reuse", SanitizeKeys.renameKey("MCorsikaEvtHeader.fNumReuse"));
        assertEquals("corsika_event_header_start_altitude", SanitizeKeys.renameKey("MCorsikaEvtHeader.fStartAltitude"));
        assertEquals("corsika_event_header_total_energy", SanitizeKeys.renameKey("MCorsikaEvtHeader.fTotalEnergy"));
        assertEquals("corsika_event_header_weighted_num_photons", SanitizeKeys.renameKey("MCorsikaEvtHeader.fWeightedNumPhotons"));
        assertEquals("corsika_event_header_x", SanitizeKeys.renameKey("MCorsikaEvtHeader.fX"));
        assertEquals("corsika_event_header_y", SanitizeKeys.renameKey("MCorsikaEvtHeader.fY"));
        assertEquals("corsika_event_header_zd", SanitizeKeys.renameKey("MCorsikaEvtHeader.fZd"));
        assertEquals("ceres_event_core_d", SanitizeKeys.renameKey("MMcEvt.fCoreD"));
        assertEquals("ceres_event_core_x", SanitizeKeys.renameKey("MMcEvt.fCoreX"));
        assertEquals("ceres_event_core_y", SanitizeKeys.renameKey("MMcEvt.fCoreY"));
        assertEquals("ceres_event_event_reuse", SanitizeKeys.renameKey("MMcEvt.fEventReuse"));
        assertEquals("ceres_event_event_number", SanitizeKeys.renameKey("MMcEvt.fEvtNumber"));
        assertEquals("ceres_event_fadc_time_jitter", SanitizeKeys.renameKey("MMcEvt.fFadcTimeJitter"));
        assertEquals("ceres_event_first_target", SanitizeKeys.renameKey("MMcEvt.fFirstTarget"));
        assertEquals("ceres_event_longi_nmax", SanitizeKeys.renameKey("MMcEvt.fLongiNmax"));
        assertEquals("ceres_event_longia", SanitizeKeys.renameKey("MMcEvt.fLongia"));
        assertEquals("ceres_event_longib", SanitizeKeys.renameKey("MMcEvt.fLongib"));
        assertEquals("ceres_event_longic", SanitizeKeys.renameKey("MMcEvt.fLongic"));
        assertEquals("ceres_event_longichi2", SanitizeKeys.renameKey("MMcEvt.fLongichi2"));
        assertEquals("ceres_event_longit0", SanitizeKeys.renameKey("MMcEvt.fLongit0"));
        assertEquals("ceres_event_longitmax", SanitizeKeys.renameKey("MMcEvt.fLongitmax"));
        assertEquals("ceres_event_electron_cherenkov_photon_fraction", SanitizeKeys.renameKey("MMcEvt.fElecCphFraction"));
        assertEquals("ceres_event_muon_cherenkov_photon_fraction", SanitizeKeys.renameKey("MMcEvt.fMuonCphFraction"));
        assertEquals("ceres_event_other_cherenkov_photon_fraction", SanitizeKeys.renameKey("MMcEvt.fOtherCphFraction"));
        assertEquals("ceres_event_pass_photon_atm", SanitizeKeys.renameKey("MMcEvt.fPassPhotAtm"));
        assertEquals("ceres_event_pass_photon_cone", SanitizeKeys.renameKey("MMcEvt.fPassPhotCone"));
        assertEquals("ceres_event_pass_photon_ref", SanitizeKeys.renameKey("MMcEvt.fPassPhotRef"));
        assertEquals("ceres_event_photo_electrons_from_shower", SanitizeKeys.renameKey("MMcEvt.fPhotElfromShower"));
        assertEquals("ceres_event_photo_electrons_in_camera", SanitizeKeys.renameKey("MMcEvt.fPhotElinCamera"));
        assertEquals("ceres_event_photon_ini", SanitizeKeys.renameKey("MMcEvt.fPhotIni"));
        assertEquals("ceres_event_thick0", SanitizeKeys.renameKey("MMcEvt.fThick0"));
        assertEquals("ceres_event_time_first", SanitizeKeys.renameKey("MMcEvt.fTimeFirst"));
        assertEquals("ceres_event_time_last", SanitizeKeys.renameKey("MMcEvt.fTimeLast"));
        assertEquals("ceres_event_z_first_interaction", SanitizeKeys.renameKey("MMcEvt.fZFirstInteraction"));
        assertEquals("ceres_event_basic_energy", SanitizeKeys.renameKey("MMcEvtBasic.fEnergy"));
        assertEquals("ceres_event_basic_impact", SanitizeKeys.renameKey("MMcEvtBasic.fImpact"));
        assertEquals("ceres_event_basic_phi", SanitizeKeys.renameKey("MMcEvtBasic.fPhi"));
        assertEquals("ceres_event_basic_theta", SanitizeKeys.renameKey("MMcEvtBasic.fTheta"));
        assertEquals("ceres_event_basic_telescope_phi", SanitizeKeys.renameKey("MMcEvtBasic.fTelescopePhi"));
        assertEquals("ceres_event_basic_telescope_theta", SanitizeKeys.renameKey("MMcEvtBasic.fTelescopeTheta"));
        assertEquals("ceres_pointing_az", SanitizeKeys.renameKey("MPointingPos.fAz"));
        assertEquals("ceres_pointing_zd", SanitizeKeys.renameKey("MPointingPos.fZd"));
        assertEquals("ceres_pointing_ra", SanitizeKeys.renameKey("MPointingPos.fRa"));
        assertEquals("ceres_pointing_dec", SanitizeKeys.renameKey("MPointingPos.fDec"));
        assertEquals("ceres_pointing_ha", SanitizeKeys.renameKey("MPointingPos.fHa"));
        assertEquals("ceres_raw_event_data_hi_gain_pix_id", SanitizeKeys.renameKey("MRawEvtData.fHiGainPixId"));
        assertEquals("ceres_raw_event_data_is_signed", SanitizeKeys.renameKey("MRawEvtData.fIsSigned"));
        assertEquals("ceres_raw_event_data_num_bytes_per_sample", SanitizeKeys.renameKey("MRawEvtData.fNumBytesPerSample"));
        assertEquals("ceres_raw_event_header_num_trig_lvl1", SanitizeKeys.renameKey("MRawEvtHeader.fNumTrigLvl1"));
        assertEquals("ceres_raw_event_header_trig_pattern", SanitizeKeys.renameKey("MRawEvtHeader.fTrigPattern"));
        assertEquals("ceres_source_az", SanitizeKeys.renameKey("MSimSourcePos.fAz"));
        assertEquals("ceres_source_dec", SanitizeKeys.renameKey("MSimSourcePos.fDec"));
        assertEquals("ceres_source_ha", SanitizeKeys.renameKey("MSimSourcePos.fHa"));
        assertEquals("ceres_source_ra", SanitizeKeys.renameKey("MSimSourcePos.fRa"));
        assertEquals("ceres_source_zd", SanitizeKeys.renameKey("MSimSourcePos.fZd"));
        assertEquals("mc_cherenkov_arrival_time_max", SanitizeKeys.renameKey("McCherArrTimeMax"));
        assertEquals("mc_cherenkov_arrival_time_mean", SanitizeKeys.renameKey("McCherArrTimeMean"));
        assertEquals("mc_cherenkov_arrival_time_min", SanitizeKeys.renameKey("McCherArrTimeMin"));
        assertEquals("mc_cherenkov_arrival_time_var", SanitizeKeys.renameKey("McCherArrTimeVar"));
        assertEquals("mc_cherenkov_photon_number", SanitizeKeys.renameKey("McCherPhotNumber"));
        assertEquals("mc_cherenkov_photon_weight", SanitizeKeys.renameKey("McCherPhotWeight"));
        assertEquals("mc_muon_cherenkov_photon_number", SanitizeKeys.renameKey("McMuonCherPhotNumber"));
        assertEquals("mc_muon_cherenkov_photon_weight", SanitizeKeys.renameKey("McMuonCherPhotWeight"));
        assertEquals("mc_noise_photon_weight", SanitizeKeys.renameKey("McNoisePhotWeight"));


    }

    @Test
    public void testProcessor() {
        SanitizeKeys sanitizeKeys = new SanitizeKeys();
        Data item = DataFactory.create();
        item.put("MCorsikaEvtHeader.fTotalEnergy", 100);
        item.put("already_snake_case", 50);

        sanitizeKeys.process(item);

        assertTrue(item.containsKey("corsika_event_header_total_energy"));
        assertTrue(item.containsKey("already_snake_case"));
        assertFalse(item.containsKey("MCorsikaEvtHeader.fTotalEnergy"));
    }

}
