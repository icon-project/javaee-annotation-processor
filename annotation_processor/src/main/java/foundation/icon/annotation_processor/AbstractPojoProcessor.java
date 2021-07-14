package foundation.icon.annotation_processor;

/**
 * Generate extend class from POJO class
 * <code>
 * class GENERATED_TYPE extend POJO {
 *  public GENERATED_TYPE(POJO) {
 *      SETTER(POJO.GETTER())
 *  }
 *  public FIELD_TYPE GETTER() {
 *      return super.GETTER()
 *  }
 *  public SETTER(FIELD_TYPE) {
 *      super.SETTER(FIELD_TYPE)
 *  }
 *  public ENCODE_TYPE toENCODE_TYPE() {
 *      return ENCODE(GETTER())
 *  }
 *  public static GENERATED_TYPE fromENCODE_TYPE(ENCODE_TYPE) {
 *      GENERATED_TYPE GENERATED = GENERATED_TYPE()
 *      SETTER(DECODE(ENCODE_TYPE))
 *      return GENERATED
 *  }
 * }
 * </code>
 */
public abstract class AbstractPojoProcessor extends AbstractProcessor {
}
