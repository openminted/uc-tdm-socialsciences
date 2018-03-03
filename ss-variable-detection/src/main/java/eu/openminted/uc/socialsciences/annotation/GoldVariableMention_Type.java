
/* First created by JCasGen Sat Mar 03 11:10:02 CET 2018 */
package eu.openminted.uc.socialsciences.annotation;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Sat Mar 03 11:10:02 CET 2018
 * @generated */
public class GoldVariableMention_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = GoldVariableMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.openminted.uc.socialsciences.annotation.GoldVariableMention");
 
  /** @generated */
  final Feature casFeat_variableId;
  /** @generated */
  final int     casFeatCode_variableId;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getVariableId(int addr) {
        if (featOkTst && casFeat_variableId == null)
      jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.GoldVariableMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_variableId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setVariableId(int addr, String v) {
        if (featOkTst && casFeat_variableId == null)
      jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.GoldVariableMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_variableId, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public GoldVariableMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_variableId = jcas.getRequiredFeatureDE(casType, "variableId", "uima.cas.String", featOkTst);
    casFeatCode_variableId  = (null == casFeat_variableId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_variableId).getCode();

  }
}



    