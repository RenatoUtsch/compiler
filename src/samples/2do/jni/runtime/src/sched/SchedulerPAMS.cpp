#include <sched/SchedulerPAMS.hpp>
#include <Task.hpp>

void SchedulerPAMS::push(std::unique_ptr<Task> task) {
    std::lock_guard<std::mutex> lock(_globalMutex);

    float speedUpCPU = task->score().gpuScore / task->score().cpuScore;
    float speedUpGPU = task->score().cpuScore / task->score().gpuScore;
    TaskInfo taskReferences;
    TaskInfoListIt cpuIt = _cpuTaskList.end();
    TaskInfoListIt gpuIt = _gpuTaskList.end();
    taskReferences.task = task.get();

    if(task->hasDeviceType(GPU)) {
        if(_gpuTaskList.empty()) {
            gpuIt = _gpuTaskList.insert(gpuIt,
                TaskInfoPair(speedUpGPU, taskReferences));
        }
        else {
            for(auto it = _gpuTaskList.begin(); it != _gpuTaskList.end(); ++it) {
                if(speedUpGPU > it->first) {
                    gpuIt = _gpuTaskList.insert(it,
                        TaskInfoPair(speedUpGPU, taskReferences));
                    break;
                }
            }

            if(gpuIt == _gpuTaskList.end()) {
                gpuIt = _gpuTaskList.insert(gpuIt,
                    TaskInfoPair(speedUpGPU, taskReferences));
            }
        }
    }

    if(task->hasDeviceType(CPU)) {
        if(_cpuTaskList.empty()) {
            cpuIt = _cpuTaskList.insert(cpuIt,
                TaskInfoPair(speedUpCPU, taskReferences));
        }
        else {
            for(auto it = _cpuTaskList.begin(); it != _cpuTaskList.end(); ++it) {
                if(speedUpCPU > it->first) {
                    cpuIt = _cpuTaskList.insert(it,
                        TaskInfoPair(speedUpCPU, taskReferences));
                    break;
                }
            }

            if(cpuIt == _cpuTaskList.end()) {
                 cpuIt = _cpuTaskList.insert(cpuIt,
                         TaskInfoPair(speedUpCPU, taskReferences));
            }
        }
    }

    if(task->hasDeviceType(CPU) && task->hasDeviceType(GPU)) {
        gpuIt->second.itCPU = cpuIt;
        gpuIt->second.itGPU = gpuIt;
        cpuIt->second.itCPU = cpuIt;
        cpuIt->second.itGPU = gpuIt;
    }
    else if(task->hasDeviceType(CPU)) {
        cpuIt->second.itCPU = cpuIt;
    }
    else /* if(task->hasDeviceType(GPU)) */ {
        gpuIt->second.itGPU = gpuIt;
    }

    task.release();
}


std::unique_ptr<Task> SchedulerPAMS::pop(Device &device) {
    std::lock_guard<std::mutex> lock(_globalMutex);
    Task *retTask = nullptr;

    if(device.type() == CPU) {
        auto it = _cpuTaskList.begin();

        if(!_cpuTaskList.empty()
                && it->second.task->hasDeviceID(device.id())) {
            retTask = it->second.task;

            if(retTask->hasDeviceType(GPU))
                _gpuTaskList.erase(it->second.itGPU);
            _cpuTaskList.erase(it->second.itCPU);
        }
        else {
            return nullptr;
        }
    }
    else if(device.type() == GPU) {
        auto it = _gpuTaskList.begin();

        if(!_gpuTaskList.empty()
                && it->second.task->hasDeviceID(device.id())) {
            retTask = it->second.task;

            if(retTask->hasDeviceType(CPU))
                _cpuTaskList.erase(it->second.itCPU);
            _gpuTaskList.erase(it->second.itGPU);
        }
        else {
            return nullptr;
        }
    }

    return std::unique_ptr<Task>(retTask);
}

bool SchedulerPAMS::hasWork() {
    std::lock_guard<std::mutex> lock(_globalMutex);
    return !_cpuTaskList.empty() || !_gpuTaskList.empty();
}
