package kidridicarus.common.role.optional;

/*
 * More properly, an enable/disable take Role.
 * Note: A Role which needs the functionality of enable but not the functionality of disable
 *   should use TriggerTakeRole, instead of this interface.
 */
public interface EnableTakeRole {
	public void onTakeEnable(boolean enabled);
}
