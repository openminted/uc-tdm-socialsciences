package webanno.custom;

/* First created by JCasGen Sun Jan 08 21:52:29 CET 2017 */

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;
import org.apache.uima.jcas.tcas.Annotation;


/**
 * Updated by JCasGen Sun Jan 08 21:52:29 CET 2017
 * @generated */
public class NamedEntity extends Annotation {
    /** @generated
     * @ordered
     */
    @SuppressWarnings ("hiding")
    public final static int typeIndexID = JCasRegistry.register(NamedEntity.class);
    /** @generated
     * @ordered
     */
    @SuppressWarnings ("hiding")
    public final static int type = typeIndexID;
    /** @generated
     * @return index of the type
     */
    @Override
    public              int getTypeIndexID() {return typeIndexID;}

    /** Never called.  Disable default constructor
     * @generated */
    protected NamedEntity() {/* intentionally empty block */}

    /** Internal - constructor used by generator
     * @generated
     * @param addr low level Feature Structure reference
     * @param type the type of this Feature Structure
     */
    public NamedEntity(int addr, TOP_Type type) {
        super(addr, type);
        readObject();
    }

    /** @generated
     * @param jcas JCas to which this Feature Structure belongs
     */
    public NamedEntity(JCas jcas) {
        super(jcas);
        readObject();
    }

    /** @generated
     * @param jcas JCas to which this Feature Structure belongs
     * @param begin offset to the begin spot in the SofA
     * @param end offset to the end spot in the SofA
     */
    public NamedEntity(JCas jcas, int begin, int end) {
        super(jcas);
        setBegin(begin);
        setEnd(end);
        readObject();
    }

    /**
     * <!-- begin-user-doc -->
     * Write your own initialization here
     * <!-- end-user-doc -->
     *
     * @generated modifiable
     */
    private void readObject() {/*default - does nothing empty block */}



    //*--------------*
    //* Feature: value

    /** getter for value - gets
     * @generated
     * @return value of the feature
     */
    public String getValue() {
        if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_value == null)
            jcasType.jcas.throwFeatMissing("value", "webanno.custom.NamedEntity");
        return jcasType.ll_cas.ll_getStringValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_value);}

    /** setter for value - sets
     * @generated
     * @param v value to set into the feature
     */
    public void setValue(String v) {
        if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_value == null)
            jcasType.jcas.throwFeatMissing("value", "webanno.custom.NamedEntity");
        jcasType.ll_cas.ll_setStringValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_value, v);}


    //*--------------*
    //* Feature: modifier

    /** getter for modifier - gets
     * @generated
     * @return value of the feature
     */
    public String getModifier() {
        if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_modifier == null)
            jcasType.jcas.throwFeatMissing("modifier", "webanno.custom.NamedEntity");
        return jcasType.ll_cas.ll_getStringValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_modifier);}

    /** setter for modifier - sets
     * @generated
     * @param v value to set into the feature
     */
    public void setModifier(String v) {
        if (NamedEntity_Type.featOkTst && ((NamedEntity_Type)jcasType).casFeat_modifier == null)
            jcasType.jcas.throwFeatMissing("modifier", "webanno.custom.NamedEntity");
        jcasType.ll_cas.ll_setStringValue(addr, ((NamedEntity_Type)jcasType).casFeatCode_modifier, v);}
}

