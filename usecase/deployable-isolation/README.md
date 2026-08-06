# Use Case — Isolate API, Kafka Worker, Job and Outbox Publisher

## Problem
A bounded context can have fast HTTP traffic, a large Kafka backlog, outbox publishing and heavy batch jobs competing for the same CPU, DB pool and JVM. A backlog or long-running job can therefore degrade unrelated API traffic.

## Candidate solutions
1. One process for everything — simplest deployment, largest shared blast radius.
2. One global platform job/outbox service — centralizes operations but couples unrelated bounded contexts and ownership.
3. Independently deployable processes per bounded context — selected default when workload evidence justifies isolation.

## Selected design
Use `be-<context>-api`, optional `-worker`, optional `-job`, optional `-outbox`. Share only bounded-context-owned modules/database contracts. Scale and recover each process independently.

## Practice
Create an inventory outbox backlog and a heavy expiry job, compare API latency/pool saturation in colocated vs isolated deployment, and save metrics/traces/results.
