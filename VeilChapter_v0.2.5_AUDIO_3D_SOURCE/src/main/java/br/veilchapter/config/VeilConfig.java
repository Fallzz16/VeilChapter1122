package br.veilchapter.config;
import br.veilchapter.VeilMod;
import net.minecraftforge.common.config.Config;
@Config(modid=VeilMod.ID, name="veilchapter")
public final class VeilConfig {
    @Config.Comment("Desabilite o sistema de anomalias sem destruir o estado salvo")
    public static boolean enableAnomalies = true;
    @Config.Comment("Habilitar aparecimento da figura preta")
    public static boolean enableWatcher = true;
    @Config.Comment("Habilitar alteracoes irreversiveis no ambiente, como incendios")
    public static boolean allowWorldDamage = false;
    @Config.Comment("Interrupcao de animais e aldeoes. Faça backup do mundo antes de ativar.")
    public static boolean enableEntityStasis = false;
    @Config.Comment("Teleporte discreto (experimental). Desativado por seguranca.")
    public static boolean enableSeamlessTeleports = false;
    @Config.Comment("Sequencia da arvore (experimental). Faça backup do mundo antes de usar.")
    public static boolean enableTreeSequence = false;
    @Config.Comment("Efeitos de escurecimento: visual + spawns proximos a cama (nao muda sistema global de skylight)")
    public static boolean enableDaySix = true;
    @Config.RangeInt(min=2,max=32)
    @Config.Comment("Distancia maxima em chunks para busca de eventos; sem carregar chunks novos")
    public static int searchChunks = 6;
}
