cd ..
cd ..
java -jar dist/MM-NEATv2.jar runNumber:%1 randomSeed:%1 base:match trials:1 maxGens:400 mu:100 io:true netio:true mating:true fs:false task:edu.utexas.cs.nn.tasks.testmatch.imagematch.ImageMatchTask log:Image-FrenchFlag saveTo:FrenchFlag matchImageFile:ImageMatchTaskFrenchFlag.png allowMultipleFunctions:true ftype:0 watch:false netChangeActivationRate:0.3 