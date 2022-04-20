# Getting Started

- create `./import/` directory and put there your `json` files
- in `run.sh` provide the java home (> 17)
- run `run.sh` it will build images and start containers
- run `call.sh` to call the API

# data reload

if you want to reload the data, then stop db container: `docker compose stop db` delete the `./neo4j/data` folder 
and start the container again

