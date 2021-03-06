/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2018, Gluu
 */
package org.gluu.oxtrust.api.test;

import java.util.List;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gluu.oxtrust.api.client.OxTrustAPIException;
import org.gluu.oxtrust.api.client.OxTrustClient;
import org.gluu.oxtrust.api.client.saml.TrustRelationshipClient;
import org.gluu.oxtrust.api.saml.SAMLTrustRelationshipShort;
import org.gluu.oxtrust.model.GluuMetadataSourceType;
import org.gluu.oxtrust.model.GluuSAMLTrustRelationship;

/**
 * SAML-related test requests.
 * 
 * @author Dmitry Ognyannikov
 */
public class SamlTestScenary {
    
    private static final Logger logger = LogManager.getLogger(SamlTestScenary.class);
    
    private final OxTrustClient client;
    
    private final Random random = new Random();
    
    public SamlTestScenary(OxTrustClient client) {
        this.client = client;
    }
    
    /**
     * Run tests.
     * 
     * @throws APITestException
     * @throws OxTrustAPIException
     */
    public void run() throws APITestException, OxTrustAPIException {
        TrustRelationshipClient samlClient = client.getTrustRelationshipClient();
        
        List<SAMLTrustRelationshipShort> trustRelationships = samlClient.list();
        
        // prevent server data corrupton - work with empty dataset
        GluuSAMLTrustRelationship trGenerated = generateRandomeSingleTrustRelationship();

        // test create()
        String inum = samlClient.create(trGenerated);

        // test read()
        GluuSAMLTrustRelationship trReaded = samlClient.read(inum);
        
        // compare etities
        if (!trGenerated.getDisplayName().equals(trReaded.getDisplayName()))
            throw new APITestException("Readed TrustRelationship isn't equal to saved");
         
        trReaded.setDescription("description changed");

        // test update()
        samlClient.update(trReaded, inum);

        // test list()
        trustRelationships = samlClient.list();
        if (!checkListForTrustRelationship(trustRelationships, inum))
            throw new APITestException("TrustRelationship really not saved");
//        
//        // test delete()
        samlClient.delete(inum);
        trustRelationships = samlClient.list();
        if (checkListForTrustRelationship(trustRelationships, inum))
            throw new APITestException("TrustRelationship really not deleted");
        
        // test list variants
        trustRelationships = samlClient.listAllActiveTrustRelationships();
        
        trustRelationships = samlClient.listAllFederations();
        
        trustRelationships = samlClient.listAllSAMLTrustRelationships(10);
        
        //trustRelationships = samlClient.listDeconstructedTrustRelationships("@!38CB.AF15.F4E4.7082!0002!9736.F2AB");
        
        // test search
        trustRelationships = samlClient.searchTrustRelationships("*", 10);
        
    }
    
    private GluuSAMLTrustRelationship generateRandomeSingleTrustRelationship() {
        int randTestNumber = Math.abs(random.nextInt());
        GluuSAMLTrustRelationship tr = new GluuSAMLTrustRelationship();
        tr.setDisplayName("test_TrustRelationship_#" + randTestNumber);
        tr.setDescription("test TrustRelationship #" + randTestNumber);
        tr.setSpMetaDataSourceType(GluuMetadataSourceType.FILE);
        tr.setUrl("https://ce.gluu.info");
        tr.setSpMetaDataFN("38CBAF15F4E4708200029736F2AB0006BF5CFB85-sp-metadata.xml");
        return tr;
    }
    
    private boolean checkListForTrustRelationship(List<SAMLTrustRelationshipShort> trustRelationships, String inum) {
        for (SAMLTrustRelationshipShort tr : trustRelationships) {
            if (inum.equals(tr.getInum()))
                return true;
        }
        return false;
    }
}
