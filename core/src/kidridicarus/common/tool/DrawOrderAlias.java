package kidridicarus.common.tool;

public class DrawOrderAlias {
	private String alias;
	private float drawOrder;

	public DrawOrderAlias(String alias, float drawOrder) {
		this.alias = alias;
		this.drawOrder = drawOrder;
	}

	public static Float getDrawOrderForAlias(DrawOrderAlias[] drawOrderAliasList, String drawOrderStr) {
		// find the value with matching alias string
		for(int i=0; i<drawOrderAliasList.length; i++) {
			if(drawOrderStr.equals(drawOrderAliasList[i].alias))
				return drawOrderAliasList[i].drawOrder;
		}
		// no value found, so return null
		return null;
	}
}
