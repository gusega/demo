CREATE CONSTRAINT FOR (n:Article) REQUIRE n.art_id IS UNIQUE;
CREATE CONSTRAINT FOR (n:Product) REQUIRE n.name IS UNIQUE;


call apoc.load.jsonArray("file:///inventory.json", "$.inventory") yield value
create (a:Article)
set a = value;

call apoc.load.jsonArray("file:///products.json", "$.products") yield value
merge  (p:Product {name: value.name})
with value, p
unwind value.contain_articles as c
match (article:Article{art_id:c.art_id})
merge (p)-[:contains_article{amount:c.amount_of}]->(article);