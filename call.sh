#! /bin/bash
curl -i 'http://localhost:8080/products'

curl -i -X 'POST' \
  'http://localhost:8080/sell' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "productsToSell": [
    {
      "name": "Dinning Table",
      "quantity": 1
    },
    {
      "name": "Dining Chair",
      "quantity": 1
    }
  ]
}'


curl -i 'http://localhost:8080/products'