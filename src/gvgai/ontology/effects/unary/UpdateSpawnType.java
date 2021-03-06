package gvgai.ontology.effects.unary;

import gvgai.core.content.InteractionContent;
import gvgai.core.game.Game;
import gvgai.core.vgdl.VGDLRegistry;
import gvgai.core.vgdl.VGDLSprite;
import gvgai.ontology.effects.Effect;
import gvgai.ontology.sprites.producer.SpawnPoint;

import java.util.ArrayList;
import java.util.Iterator;

public class UpdateSpawnType extends Effect {

    public String stype; //new missile to replace sprite2
    public String spawnPoint; //stype string of spawn point
    public int itype, ispawn;

    public UpdateSpawnType(InteractionContent cnt)
    {
        this.parseParameters(cnt);
        itype = VGDLRegistry.GetInstance().getRegisteredSpriteValue(stype);
        ispawn = VGDLRegistry.GetInstance().getRegisteredSpriteValue(spawnPoint);
    }

    @Override
    public void execute(VGDLSprite sprite1, VGDLSprite sprite2, Game game)
    {
        ArrayList<Integer> subtypes = game.getSubTypes(ispawn);
        for (Integer i: subtypes) {
            Iterator<VGDLSprite> spriteIt = game.getSpriteGroup(i);
            if (spriteIt != null) while (spriteIt.hasNext()) {
                try {
                    VGDLSprite sp = spriteIt.next();
                    SpawnPoint s = (SpawnPoint) sp;
                    s.updateItype(sprite2.getType(), itype);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @Override
    public ArrayList<String> getEffectSprites(){
    	ArrayList<String> result = new ArrayList<String>();
    	if(stype!=null) result.add(stype);
    	
    	return result;
    }
}
