cd ..
cd ..
java -jar dist/MM-NEATv2.jar runNumber:%1 randomSeed:%1 base:vizdoomhealthgather trials:5 maxGens:500 mu:10 io:true netio:true mating:true task:edu.utexas.cs.nn.tasks.vizdoom.VizDoomHealthGatherTask cleanOldNetworks:true fs:false noisyTaskStat:edu.utexas.cs.nn.util.stats.Average log:HealthGather-Control saveTo:Control gameWad:freedoom2.wad doomEpisodeLength:10000 doomInputStartX:0 doomInputStartY:75 doomInputHeight:75 doomInputWidth:200 doomInputPixelSmudge:9 doomSmudgeStat:edu.utexas.cs.nn.util.stats.MostExtreme moVizDoom:true doomInputColorVal:0 printFitness:true