# Changeme Later


## Unpackage the stupid dependencies because Java
### Steps
- From the parent directory, run `java xf ./commons-lang3-3.12.0.jar`
- Build the project with `javac ./searchclient/*.java`
- Run the level you want, for example: `java -jar mavis.jar -l ./levels/MAPF00.lvl -c "java -Xmx16g searchclient.SearchClient -wastar" -g -s 250 -t 180`