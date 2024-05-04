# Change Log

## [Unreleased]

### Breaking

* Upon request, introduced a new API that uses kotlin native types and does not include Arrow as a dependency.
  The original lib is renamed `lib-arrow`.

## [0.3.0]

### Breaking

* Refined type aliases on Transitioner so that implementations are free to define a base transition type that may
  implement common functionality. For example, a new base transition type can define a common way to execute
  side-effects that occur in pre and post hook transitioner functions. See TransitionerTest use of 
  `specificToThisTransitionType` for an example.

## [0.2.0]

### Breaking

* Changes to new API's method signatures and types required to integrate with its first real project. 

## [0.1.0]

### Breaking

* `StateMachine.verify` no longer requires a second argument to declare the base type of the state machine. This is now
  inferred from the first argument.`

### Added

* `StateMachine.mermaid` is a new utility that will generate mermaid diagram from your state machine.
* New simplified API is being introduced to make it easier to use the library. This can be found in the package
  `app.cash.kfsm`. It is not yet ready for production use, but we are looking for feedback on the new API.


## [0.0.2] - 2023-09-11

### Added

* Initial release from internal

