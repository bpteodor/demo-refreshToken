# demo RefreshToken CLI

## run locally

```
java -jar target/app-jar-with-dependencies.jar -cs=<client-secret> <refresh-token>
```

## run with docker
```
docker run bpteodor/drt -cs=<client-secret> <refresh-token>
docker run bpteodor/drt -wt=PT10s -cid=<client-id> -cs=<client-secret> <refresh-token>
```