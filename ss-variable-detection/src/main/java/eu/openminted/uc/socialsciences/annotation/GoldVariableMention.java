

/* First created by JCasGen Sat Mar 03 11:10:02 CET 2018 */
package eu.openminted.uc.socialsciences.annotation;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sat Mar 03 11:10:02 CET 2018
 * XML source: /Users/bluefire/git/uc-tdm-socialsciences/ss-variable-detection/src/main/resources/desc/type/variable-type.xml
 * @generated */
public class GoldVariableMention extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(GoldVariableMention.class);
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
  protected GoldVariableMention() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public GoldVariableMention(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public GoldVariableMention(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public GoldVariableMention(JCas jcas, int begin, int end) {
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
  //* Feature: variableId

  /** getter for variableId - gets 
   * @generated
   * @return value of the feature 
   */
  public String getVariableId() {
    if (GoldVariableMention_Type.featOkTst && ((GoldVariableMention_Type)jcasType).casFeat_variableId == null)
      jcasType.jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.GoldVariableMention");
    return jcasType.ll_cas.ll_getStringValue(addr, ((GoldVariableMention_Type)jcasType).casFeatCode_variableId);}
    
  /** setter for variableId - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setVariableId(String v) {
    if (GoldVariableMention_Type.featOkTst && ((GoldVariableMention_Type)jcasType).casFeat_variableId == null)
      jcasType.jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.GoldVariableMention");
    jcasType.ll_cas.ll_setStringValue(addr, ((GoldVariableMention_Type)jcasType).casFeatCode_variableId, v);}    
  }

    