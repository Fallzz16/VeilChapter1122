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
    @Config.Comment("Watcher observando a distancia: eventos mais frequentes sem forcar chunks novos.")
    public static boolean enableDistantWatcher = true;
    @Config.Comment("Olhos e cabeca espreitando atras de blocos; aparicoes leves e sem colisao.")
    public static boolean enablePeeking = true;
    @Config.Comment("Chance de Watcher durante sono, somente se ainda estiver dormindo apos 1 segundo.")
    public static boolean enableBedWatcher = true;
    @Config.RangeInt(min=5,max=65)
    @Config.Comment("Chance percentual por tentativa de dormir, dias narrativos 2 a 7.")
    public static int bedWatcherChance = 24;
    @Config.RangeInt(min=24,max=72)
    @Config.Comment("Distancia maxima aproximada do Watcher distante, respeitando chunks carregados.")
    public static int watcherHorizonBlocks = 40;
    @Config.RangeInt(min=2,max=32)
    @Config.Comment("Distancia maxima em chunks para busca de eventos; sem carregar chunks novos")
    public static int searchChunks = 6;
}
