package io.github.jorelali.commandapi.api.wrappers;

public final class ItemSlot {

	enum ContainerType {
		ARMOR_FEET, ARMOR_LEGS, ARMOR_CHEST, ARMOR_HEAD,
		HORSE_SADDLE, HORSE_ARMOR, HORSE_CHEST,
		WEAPON_MAINHAND,WEAPON_OFFHAND,
		CONTAINER, //Includes Hotbar & Inventory
		ENDERCHEST,
		HORSE,
		VILLAGER
	}
	
	private final int index;
	private final ContainerType containerType;
	
	public ItemSlot(int id) {
		if (id >= 0 && id <= 53) {
			/* Note: Container value 40 still implies offhand btw.
			 * Therefore, if index > 35 && CONTAINER, check if
			 * inventory is a doublechest!
			 */
			containerType = ContainerType.CONTAINER;
			index = id;
		}
		else if (id >= 200 && id <= 226) {
			//TODO: Test this.
			containerType = ContainerType.ENDERCHEST;
			index = id - 200;
		}
		else if (id >= 300 && id <= 307) {
			containerType = ContainerType.VILLAGER;
			index = id - 300;
		}
		else if (id >= 500 && id <= 514) {
			containerType = ContainerType.HORSE;
			index = id - 500;
		}
		else {
			index = 0;
			switch (id) {
				case 98:  containerType = ContainerType.WEAPON_MAINHAND; break;
				case 99:  containerType = ContainerType.WEAPON_OFFHAND;  break;
				
				case 100: containerType = ContainerType.ARMOR_FEET;      break;
				case 101: containerType = ContainerType.ARMOR_LEGS;      break;
				case 102: containerType = ContainerType.ARMOR_CHEST;     break;
				case 103: containerType = ContainerType.ARMOR_HEAD;      break;
				
				case 400: containerType = ContainerType.HORSE_SADDLE;    break;
				case 401: containerType = ContainerType.HORSE_ARMOR;     break;
				case 499: containerType = ContainerType.HORSE_CHEST;     break;
				default:  containerType = null;
			}
		}
	}

	public ContainerType getContainerType() {
		return containerType;
	}

	public int getIndex() {
		return index;
	}
	
}
