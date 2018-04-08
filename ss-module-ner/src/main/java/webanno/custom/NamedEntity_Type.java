
/* First created by JCasGen Sun Jan 08 22:44:16 CET 2017 */
package webanno.custom;

import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.tcas.Annotation_Type;

/**
 * Updated by JCasGen Sun Jan 08 22:44:16 CET 2017
 * @generated */
public class NamedEntity_Type extends Annotation_Type {
    /** @generated */
    @SuppressWarnings ("hiding")
    public final static int typeIndexID = NamedEntity.typeIndexID;
    /** @generated
     @modifiable */
    @SuppressWarnings ("hiding")
    public final static boolean featOkTst = JCasRegistry.getFeatOkTst("webanno.custom.NamedEntity");

    /** @generated */
    final Feature casFeat_value;
    /** @generated */
    final int     casFeatCode_value;
    /** @generated
     * @param addr low level Feature Structure reference
     * @return the feature value
     */
    public String getValue(int addr) {
        if (featOkTst && casFeat_value == null)
            jcas.throwFeatMissing("value", "webanno.custom.NamedEntity");
        return ll_cas.ll_getStringValue(addr, casFeatCode_value);
    }
    /** @generated
     * @param addr low level Feature Structure reference
     * @param v value to set
     */
    public void setValue(int addr, String v) {
        if (featOkTst && casFeat_value == null)
            jcas.throwFeatMissing("value", "webanno.custom.NamedEntity");
        ll_cas.ll_setStringValue(addr, casFeatCode_value, v);}



    /** @generated */
    final Feature casFeat_modifier;
    /** @generated */
    final int     casFeatCode_modifier;
    /** @generated
     * @param addr low level Feature Structure reference
     * @return the feature value
     */
    public String getModifier(int addr) {
        if (featOkTst && casFeat_modifier == null)
            jcas.throwFeatMissing("modifier", "webanno.custom.NamedEntity");
        return ll_cas.ll_getStringValue(addr, casFeatCode_modifier);
    }
    /** @generated
     * @param addr low level Feature Structure reference
     * @param v value to set
     */
    public void setModifier(int addr, String v) {
        if (featOkTst && casFeat_modifier == null)
            jcas.throwFeatMissing("modifier", "webanno.custom.NamedEntity");
        ll_cas.ll_setStringValue(addr, casFeatCode_modifier, v);}





    /** initialize variables to correspond with Cas Type and Features
     * @generated
     * @param jcas JCas
     * @param casType Type
     */
    public NamedEntity_Type(JCas jcas, Type casType) {
        super(jcas, casType);
        casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());


        casFeat_value = jcas.getRequiredFeatureDE(casType, "value", "uima.cas.String", featOkTst);
        casFeatCode_value  = (null == casFeat_value) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_value).getCode();


        casFeat_modifier = jcas.getRequiredFeatureDE(casType, "modifier", "uima.cas.String", featOkTst);
        casFeatCode_modifier  = (null == casFeat_modifier) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_modifier).getCode();

    }
}