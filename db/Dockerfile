FROM neo4j:latest

COPY entrypoint.sh ./
COPY scripts/ scripts/

ENTRYPOINT ["tini", "-g", "--", "./entrypoint.sh"]
CMD ["neo4j"]