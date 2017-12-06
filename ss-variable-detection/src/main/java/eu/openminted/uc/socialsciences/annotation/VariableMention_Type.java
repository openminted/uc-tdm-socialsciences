
/* First created by JCasGen Thu Nov 30 11:52:53 CET 2017 */
package eu.openminted.uc.socialsciences.annotation;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Nov 30 11:52:53 CET 2017
 * @generated */
public class VariableMention_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = VariableMention.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("eu.openminted.uc.socialsciences.annotation.VariableMention");
 
  /** @generated */
  final Feature casFeat_correct;
  /** @generated */
  final int     casFeatCode_correct;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getCorrect(int addr) {
        if (featOkTst && casFeat_correct == null)
      jcas.throwFeatMissing("correct", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_correct);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setCorrect(int addr, String v) {
        if (featOkTst && casFeat_correct == null)
      jcas.throwFeatMissing("correct", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_correct, v);}
    
  
 
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
      jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_variableId);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setVariableId(int addr, String v) {
        if (featOkTst && casFeat_variableId == null)
      jcas.throwFeatMissing("variableId", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_variableId, v);}
    
  
 
  /** @generated */
  final Feature casFeat_label;
  /** @generated */
  final int     casFeatCode_label;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLabel(int addr) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_label);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabel(int addr, String v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_label, v);}
    
  
 
  /** @generated */
  final Feature casFeat_question;
  /** @generated */
  final int     casFeatCode_question;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getQuestion(int addr) {
        if (featOkTst && casFeat_question == null)
      jcas.throwFeatMissing("question", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_question);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setQuestion(int addr, String v) {
        if (featOkTst && casFeat_question == null)
      jcas.throwFeatMissing("question", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_question, v);}
    
  
 
  /** @generated */
  final Feature casFeat_subQuestion;
  /** @generated */
  final int     casFeatCode_subQuestion;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getSubQuestion(int addr) {
        if (featOkTst && casFeat_subQuestion == null)
      jcas.throwFeatMissing("subQuestion", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_subQuestion);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setSubQuestion(int addr, String v) {
        if (featOkTst && casFeat_subQuestion == null)
      jcas.throwFeatMissing("subQuestion", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_subQuestion, v);}
    
  
 
  /** @generated */
  final Feature casFeat_answers;
  /** @generated */
  final int     casFeatCode_answers;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getAnswers(int addr) {
        if (featOkTst && casFeat_answers == null)
      jcas.throwFeatMissing("answers", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    return ll_cas.ll_getStringValue(addr, casFeatCode_answers);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setAnswers(int addr, String v) {
        if (featOkTst && casFeat_answers == null)
      jcas.throwFeatMissing("answers", "eu.openminted.uc.socialsciences.annotation.VariableMention");
    ll_cas.ll_setStringValue(addr, casFeatCode_answers, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public VariableMention_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_correct = jcas.getRequiredFeatureDE(casType, "correct", "uima.cas.String", featOkTst);
    casFeatCode_correct  = (null == casFeat_correct) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_correct).getCode();

 
    casFeat_variableId = jcas.getRequiredFeatureDE(casType, "variableId", "uima.cas.String", featOkTst);
    casFeatCode_variableId  = (null == casFeat_variableId) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_variableId).getCode();

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.String", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

 
    casFeat_question = jcas.getRequiredFeatureDE(casType, "question", "uima.cas.String", featOkTst);
    casFeatCode_question  = (null == casFeat_question) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_question).getCode();

 
    casFeat_subQuestion = jcas.getRequiredFeatureDE(casType, "subQuestion", "uima.cas.String", featOkTst);
    casFeatCode_subQuestion  = (null == casFeat_subQuestion) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_subQuestion).getCode();

 
    casFeat_answers = jcas.getRequiredFeatureDE(casType, "answers", "uima.cas.String", featOkTst);
    casFeatCode_answers  = (null == casFeat_answers) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_answers).getCode();

  }
}



    